/*
Copyright (C) 2016 Pierre-François Gimenez

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

import obstacles.memory.ObstaclesIteratorPresent;
import obstacles.types.ObstacleCircular;
import obstacles.types.ObstacleRectangular;
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
import pathfinding.astarCourbe.arcs.ArcCourbe;

/**
 * S'occupe de la trajectoire actuelle.
 * Notifie dès qu'un chemin (partiel ou complet) est disponible
 * @author pf
 *
 */

// TODO : le premier point du chemin doit être la position actuelle du robot

public class CheminPathfinding implements Service, Printable, Configurable
{
	protected Log log;
	private BufferOutgoingOrder out;
	private ObstaclesIteratorPresent iterObstacles;
	private IteratorCheminPathfinding iterChemin;	
	private PrintBuffer buffer;
	
	private volatile CinematiqueObs[] chemin = new CinematiqueObs[256];
	protected int indexFirst = 0; // indice du point en cours
	protected int indexLast = 0; // indice du prochain point de la trajectoire (donc indexLast - 1 est l'index du dernier point accessible)
	private int lastValidIndex; // l'indice du dernier index (-1 si aucun ne l'est, Integer.MAX_VALUE si tous le sont)
	private boolean uptodate = true; // le chemin est-il complet
	private int margeNecessaire;
	private int anticipationReplanif;
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
	public void checkColliding()
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
		iterObstacles.save(); // on sauvegarde sa position actuelle, c'est-à-dire la première position des nouveaux obstacles
		iterChemin.reinit();
		lastValidIndex = -1;
		
		while(iterChemin.hasNext())
		{
			ObstacleRectangular a = iterChemin.next().obstacle;
			iterObstacles.load();
			while(iterObstacles.hasNext())
			{
				if(iterObstacles.next().isColliding(a))
				{
					while(iterObstacles.hasNext()) // on s'assure que l'iterateur se termine à la fin des obstacles connus
						iterObstacles.next();
					return true;
				}
			}
			
			/**
			 * Mise à jour de lastValidIndex
			 */
			lastValidIndex = iterChemin.getIndex();
			if(minus(lastValidIndex, add(indexFirst, margeNecessaire)) >= anticipationReplanif)
				lastValidIndex = minus(lastValidIndex, anticipationReplanif);
			else
				lastValidIndex = add(indexFirst, margeNecessaire);
			
		}
		return false;
	}
	
	@Override
	public void useConfig(Config config)
	{
		margeNecessaire = config.getInt(ConfigInfo.PF_MARGE_NECESSAIRE);
		anticipationReplanif = config.getInt(ConfigInfo.PF_ANTICIPATION);
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
	public void add(ArcCourbe arc)
	{
		synchronized(this)
		{
			int tmp = indexLast;
			for(int i = 0; i < arc.getNbPoints(); i++)
				add(arc.getPoint(i));
			out.envoieArcCourbe(arc, tmp);
		}
		if(graphic)
			synchronized(buffer)
			{
				buffer.notify();
			}
	}
	
	protected synchronized CinematiqueObs get(int index)
	{
		if(minus(index, indexFirst) < minus(indexLast, indexFirst))
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
	 * @return
	 */
	public Cinematique getLastValidCinematique()
	{
		if(lastValidIndex == -1)
			return null;
		indexLast = lastValidIndex + 1; // on complètera à partir de ce point
		return chemin[lastValidIndex];
	}

	@Override
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
		iterChemin.reinit();
		while(iterChemin.hasNext())
		{
			Cinematique a = iterChemin.next();
			buffer.addSupprimable(new ObstacleCircular(a.getPosition(), 8, Couleur.TRAJECTOIRE.couleur));
		}
	}

	@Override
	public Layer getLayer()
	{
		return Layer.FOREGROUND;
	}

	public int minus(int indice1, int indice2)
	{
		return (indice1 - indice2 + 256) & 0xFF;
	}
	
	public int add(int indice1, int indice2)
	{
		return (indice1 + indice2) & 0xFF;
	}
	
}
