/*
Copyright (C) 2013-2017 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package pathfinding.chemin;

import java.awt.Graphics;
import java.util.LinkedList;

import obstacles.memory.ObstaclesIteratorPresent;
import obstacles.types.ObstacleCircular;
import obstacles.types.ObstacleProximity;
import obstacles.types.ObstacleRobot;
import graphic.Fenetre;
import graphic.PrintBuffer;
import graphic.printable.Couleur;
import graphic.printable.Layer;
import graphic.printable.Printable;
import robot.Cinematique;
import robot.CinematiqueObs;
import robot.RobotReal;
import serie.BufferOutgoingOrder;
import utils.Log;
import config.Config;
import config.ConfigInfo;
import config.Configurable;
import container.Service;
import exceptions.PathfindingException;

/**
 * S'occupe de la trajectoire actuelle.
 * Notifie dès qu'un chemin (partiel ou complet) est disponible
 * @author pf
 *
 */

public class CheminPathfinding implements Service, Printable, Configurable
{
	protected Log log;
	private BufferOutgoingOrder out;
	private ObstaclesIteratorPresent iterObstacles;
	private IteratorCheminPathfinding iterChemin;	
	private PrintBuffer buffer;
	
	private volatile CinematiqueObs[] chemin = new CinematiqueObs[256];
	private volatile ObstacleCircular[] aff = new ObstacleCircular[256];
	protected int indexFirst = 0; // indice du point en cours
	protected int indexLast = 0; // indice du prochain point de la trajectoire (donc indexLast - 1 est l'index du dernier point accessible)
	private int lastValidIndex = -1; // l'indice du dernier index (-1 si aucun ne l'est, Integer.MAX_VALUE si tous le sont)
	private boolean uptodate = true; // le chemin est-il complet
	private int margeNecessaire, margeInitiale;
	private boolean graphic;
	
	public CheminPathfinding(Log log, BufferOutgoingOrder out, ObstaclesIteratorPresent iterator, PrintBuffer buffer, RobotReal robot)
	{
		for(int i = 0; i < chemin.length; i++)
			chemin[i] = new CinematiqueObs(robot);
		this.log = log;
		this.out = out;
		this.iterObstacles = iterator;
		iterChemin = new IteratorCheminPathfinding(this);
		this.buffer = buffer;
	}
	
	/**
	 * A-t-on besoin d'un chemin partiel ?
	 * @return
	 */
	public boolean needPartial()
	{
		return !uptodate && minus(indexLast, indexFirst) < margeNecessaire;
	}
	
	/**
	 * Y a-t-il une collision avec un obstacle de proximité ?
	 */
	public synchronized void checkColliding()
	{
		if(isColliding())
		{
			uptodate = false;
			notify();
		}
	}
	
	/**
	 * Vérifie des collisions et met à jour lastIndex
	 * On ne vérifie la collision qu'avec les obstacles de proximité
	 * On suppose qu'il n'y a pas de collision avec les autres éléments
	 * @return
	 */
	private boolean isColliding()
	{
		iterChemin.reinit();
		lastValidIndex = -1; // reste à -1 à moins d'avoir assez de points pour le bas niveau
		int firstPossible = add(indexFirst, margeInitiale);
		while(iterChemin.hasNext())
		{
			ObstacleRobot a = iterChemin.next().obstacle;
			iterObstacles.reinit();
			while(iterObstacles.hasNext())
			{
				ObstacleProximity o = iterObstacles.next();
				if(o.isColliding(a))
				{
//					log.debug(o+" collisionne le robot en "+a);
					return true;
				}
			}
			
			/**
			 * Mise à jour de lastValidIndex
			 */
			int tmp = iterChemin.getIndex();
			if(minus(tmp, firstPossible) >= 0)
				lastValidIndex = firstPossible; // on reprendra d'ici
		}
		return false;
	}
	
	@Override
	public void useConfig(Config config)
	{
		margeNecessaire = config.getInt(ConfigInfo.PF_MARGE_NECESSAIRE);
		margeInitiale = config.getInt(ConfigInfo.PF_MARGE_INITIALE);
		graphic = config.getBoolean(ConfigInfo.GRAPHIC_TRAJECTORY_FINAL);
		if(graphic)
			buffer.add(this);
	}
	
	/**
	 * Le chemin est-il vide ?
	 * TODO inutilisé ?
	 * @return
	 */
	public synchronized boolean isEmpty()
	{
		return indexFirst == indexLast;
	}
	
	private void add(CinematiqueObs c)
	{
		c.copy(chemin[indexLast]);
		indexLast = add(indexLast, 1);
		
		// si on revient au début, c'est qu'il y a un problème ou que le buffer est sous-dimensionné
		if(indexLast == indexFirst)
			log.critical("Buffer trop petit !");
	}

	/**
	 * Ajoute un arc au chemin
	 * Il sera directement envoyé à la série
	 * @param arc
	 */
	public void add(LinkedList<CinematiqueObs> points) throws PathfindingException
	{
		/*
		 * En cas de replanification, si les points ajoutés ne suffisent pas pour avoir assurer la marge du bas niveau, on lance une exception
		 */
		if(!uptodate && minus(add(indexLast, points.size()), indexFirst) < margeNecessaire)
			throw new PathfindingException("Pas assez de points pour le bas niveau");
				
		if(!points.isEmpty())
		{
			synchronized(this)
			{
				int tmp = indexLast;
				for(CinematiqueObs p : points)
					add(p);
				if(isIndexValid(tmp - 1) && chemin[tmp - 1].enMarcheAvant == points.getFirst().enMarcheAvant)
				{
					points.addFirst(chemin[tmp - 1]); // on renvoie ce point afin qu'il ne soit plus un point d'arrêt
					tmp--;
				}
				out.envoieArcCourbe(points, tmp);
			}
			if(graphic)
				synchronized(buffer)
				{
					buffer.notify();
				}
		}
	}
	
	private boolean isIndexValid(int index)
	{
		return minus(index, indexFirst) < minus(indexLast, indexFirst);
	}
	
	protected synchronized CinematiqueObs get(int index)
	{
		if(isIndexValid(index))
			return chemin[index];
		return null;
	}

	/**
	 * Supprime complètement le trajet en cours
	 */
	public void clear()
	{
		/**
		 * Parfois, le plus simple est de s'arrêter et de réfléchir sur sa vie
		 */
		out.immobilise();
		uptodate = true;
		indexLast = indexFirst;
		
		if(graphic)
			synchronized(buffer)
			{
				buffer.notify();
			}
	}
	
	/**
	 * Doit être mise à "true" si le trajet est entièrement planifié
	 * @param uptodate
	 */
	public void setUptodate(boolean uptodate)
	{
		this.uptodate = uptodate;
	}
	
	public boolean isUptodate()
	{
		return uptodate;
	}

	/**
	 * Mise à jour, depuis le bas niveau, de la cinématique actuelle
	 * Renvoie la cinématique actuelle
	 * @param indexTrajectory
	 */
	public Cinematique setCurrentIndex(int indexTrajectory)
	{
		synchronized(this)
		{
			indexFirst = indexTrajectory;
		}
		if(graphic)
			synchronized(buffer)
			{
				buffer.notify();
			}
		synchronized(this)
		{
			return chemin[indexFirst];
		}
	}
	
	/**
	 * Renvoie l'arc du dernier point qu'on peut encore utiliser
	 * Un "checkColliding" doit être fait avant !
	 * @return
	 */
	public Cinematique getLastValidCinematique() throws PathfindingException
	{
		if(lastValidIndex == -1)
			throw new PathfindingException("Pas assez de points pour le bas niveau !");
		
		indexLast = lastValidIndex + 1; // on complètera à partir de ce point
		return chemin[lastValidIndex];
	}

	@Override
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
//		for(int i = 0; i < 256; i++)
//			if(aff[i] != null)
//				buffer.removeSupprimable(aff[i]);
		iterChemin.reinit();
		while(iterChemin.hasNext())
		{
			Cinematique a = iterChemin.next();
			aff[iterChemin.getIndex()] = new ObstacleCircular(a.getPosition(), 8, Couleur.TRAJECTOIRE);
			buffer.addSupprimable(aff[iterChemin.getIndex()]);
		}
	}

	@Override
	public Layer getLayer()
	{
		return Layer.FOREGROUND;
	}

	public final int minus(int indice1, int indice2)
	{
		return (indice1 - indice2 + 256) & 0xFF;
	}
	
	public final int add(int indice1, int indice2)
	{
		return (indice1 + indice2) & 0xFF;
	}
	
}
