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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import obstacles.memory.ObstaclesIteratorPresent;
import obstacles.types.ObstacleCircular;
import obstacles.types.ObstacleProximity;
import obstacles.types.ObstacleRobot;
import pathfinding.astar.arcs.ClothoidesComputer;
import graphic.PrintBufferInterface;
import graphic.printable.Couleur;
import graphic.printable.Segment;
import robot.Cinematique;
import robot.CinematiqueObs;
import serie.BufferOutgoingOrder;
import serie.Ticket;
import utils.Log;
import utils.Log.Verbose;
import utils.Vec2RO;
import config.Config;
import config.ConfigInfo;
import container.Service;
import container.dependances.HighPFClass;
import exceptions.PathfindingException;

/**
 * S'occupe de la trajectoire actuelle.
 * Notifie dès qu'un chemin (partiel ou complet) est disponible
 * @author pf
 *
 */

public class CheminPathfinding implements Service, HighPFClass, CheminPathfindingInterface
{
	protected Log log;
	private BufferOutgoingOrder out;
	private ObstaclesIteratorPresent iterObstacles;
	private IteratorCheminPathfinding iterChemin;	
	private IteratorCheminPathfinding iterCheminPrint;
	private PrintBufferInterface buffer;
	private List<Ticket> tickets = new ArrayList<Ticket>();
	
	private volatile CinematiqueObs[] chemin = new CinematiqueObs[256];
	private volatile ObstacleCircular[] aff = new ObstacleCircular[256];
	private volatile Segment[] affSeg = new Segment[256];
	protected volatile int indexFirst = 0; // indice du point en cours
	protected volatile int indexLast = 0; // indice du prochain point de la trajectoire (donc indexLast - 1 est l'index du dernier point accessible)
	private volatile boolean uptodate = true; // le chemin est-il complet
	private volatile boolean empty = false;
	private volatile boolean needRestart = false; // faut-il recalculer d'un autre point ?
	private int margeNecessaire, margeInitiale, margeAvantCollision;
	private boolean graphic;
	
	public CheminPathfinding(Log log, BufferOutgoingOrder out, ObstaclesIteratorPresent iterator, PrintBufferInterface buffer, Config config)
	{
		this.log = log;
		this.out = out;
		this.iterObstacles = iterator;
		iterChemin = new IteratorCheminPathfinding(this);
		iterCheminPrint = new IteratorCheminPathfinding(this);
		this.buffer = buffer;

		int demieLargeurNonDeploye = config.getInt(ConfigInfo.LARGEUR_NON_DEPLOYE)/2;
		int demieLongueurArriere = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE);
		int demieLongueurAvant = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_AVANT);
		int marge = config.getInt(ConfigInfo.DILATATION_OBSTACLE_ROBOT);
		margeNecessaire = (int)(config.getDouble(ConfigInfo.PF_MARGE_NECESSAIRE)/ClothoidesComputer.PRECISION_TRACE_MM);
		margeAvantCollision = (int)(config.getInt(ConfigInfo.PF_MARGE_AVANT_COLLISION)/ClothoidesComputer.PRECISION_TRACE_MM);
		margeInitiale = (int)(config.getInt(ConfigInfo.PF_MARGE_INITIALE)/ClothoidesComputer.PRECISION_TRACE_MM);
		graphic = config.getBoolean(ConfigInfo.GRAPHIC_TRAJECTORY_FINAL);

		for(int i = 0; i < chemin.length; i++)
			chemin[i] = new CinematiqueObs(demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant, marge);
	}
	
	/**
	 * Renvoie l'état (marche avant ou arrière) du prochain point qui sera parcouru
	 * @return
	 */
	public boolean getNextMarcheAvant()
	{
		return chemin[add(indexFirst,1)].enMarcheAvant;
	}
	
	@Override
	public boolean needStop()
	{
		return !uptodate && empty;
	}
	
	@Override
	public boolean aAssezDeMarge()
	{
		boolean out = uptodate || minus(indexLast, indexFirst) >= margeNecessaire;
		if(!out)
			log.warning("Replanification partielle nécessaire : "+minus(indexLast, indexFirst)+" points d'avance seulement.", Verbose.REPLANIF.masque);
		return out;
	}
	
	/**
	 * Y a-t-il une collision avec un obstacle de proximité ?
	 */
	public synchronized void checkColliding(boolean all)
	{
		if(!empty && isColliding(all))
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
	private synchronized boolean isColliding(boolean all)
	{
		iterChemin.reinit();
		boolean assezDeMargeDepuisDepart = false;
		int nbMarge = 0;
		int firstPossible = add(indexFirst, margeInitiale); // le premier point qu'on pourrait accepter
		
		if(all)
			iterObstacles.reinit();
		iterObstacles.save();
		while(iterChemin.hasNext())
		{
			CinematiqueObs cinem = iterChemin.next();
			int current = iterChemin.getIndex();
			ObstacleRobot a = cinem.obstacle;
			iterObstacles.load();
			while(iterObstacles.hasNext())
			{
				ObstacleProximity o = iterObstacles.next();
				if(o.isColliding(a))
				{
					log.debug("Collision en "+current+". Actuel : "+indexFirst+" (soit environ "+ClothoidesComputer.PRECISION_TRACE_MM*(minus(current, indexFirst)-0.5)+" mm avant impact)", Verbose.CAPTEURS.masque | Verbose.REPLANIF.masque);

					// on n'a pas assez de marge !
					if(!assezDeMargeDepuisDepart)
					{
						log.warning("Pas assez de marge !", Verbose.REPLANIF.masque);
						indexLast = indexFirst;
						empty = true;
					}
					else
					{
						log.warning("Replanification nécessaire", Verbose.REPLANIF.masque);
						// on a assez de marge, on va faire de la replanification à la volée
						indexLast = minus(current, Math.min(nbMarge, margeAvantCollision));
						out.makeNextObsolete(chemin[minus(indexLast,1)], minus(indexLast,1));
						log.debug("On raccourcit la trajectoire. IndexLast = "+indexLast, Verbose.REPLANIF.masque);
						needRestart = true;
					}
					// on va jusqu'au bout de l'itérateur
					while(iterObstacles.hasNext())
						iterObstacles.next();
					return true;
				}
			}
			
			// on a pu aller jusqu'à firstPossible sans rencontrer d'obstacle : on peut replanifier
			if(assezDeMargeDepuisDepart)
				nbMarge++;
			
			if(current == firstPossible)
				assezDeMargeDepuisDepart = true;
		}
		return false;
	}
	
	public synchronized boolean isArrived()
	{
		return uptodate && minus(indexLast, indexFirst) < 2;
	}
	
	/**
	 * Le chemin est-il vide ?
	 * @return
	 */
	public boolean isEmpty()
	{
		return empty;
	}
	
	private void addToEnd(CinematiqueObs c)
	{
		c.copy(chemin[indexLast]);
		indexLast = add(indexLast, 1);
		empty = false;
		// si on revient au début, c'est qu'il y a un problème ou que le buffer est sous-dimensionné
		if(indexLast == indexFirst)
			log.critical("Buffer trop petit !");
	}
	
	public void waitTrajectoryTickets() throws InterruptedException
	{
		Iterator<Ticket> iter = tickets.iterator();
		while(iter.hasNext())
		{
			iter.next().attendStatus();
			iter.remove();
		}
	}

	/**
	 * Ajoute un arc au chemin
	 * Il sera directement envoyé à la série
	 * @param arc
	 */
	@Override
	public void addToEnd(LinkedList<CinematiqueObs> points) throws PathfindingException
	{
		/*
		 * En cas de replanification, si les points ajoutés ne suffisent pas pour avoir assurer la marge du bas niveau, on lance une exception
		 */
		log.debug("Ajout de "+points.size()+" points. Marge : "+minus(add(indexLast, points.size()), indexFirst)+". First : "+indexFirst+", last = "+indexLast);
		
		if(!uptodate && minus(add(indexLast, points.size()), indexFirst) < margeNecessaire)
			throw new PathfindingException("Pas assez de points pour le bas niveau");
				
		if(!points.isEmpty())
		{
			int tmp = minus(indexLast,1); // index du dernier point envoyé
			for(CinematiqueObs p : points)
				addToEnd(p);

			if(isIndexValid(tmp))
				points.addFirst(chemin[tmp]); // on renvoie ce point afin qu'il ne soit plus un point d'arrêt
			else
				tmp = add(tmp, 1); // on ne le renvoie pas

			Ticket[] ts = out.envoieArcCourbe(points, tmp);
			for(Ticket t : ts)
				tickets.add(t);

			if(graphic)
				updateAffichage();

			checkColliding(true); // on vérifie que les points ajoutés ne collisionnent pas
		}
		
/*		iterCheminPrint.reinit();
		log.debug("Affichage du chemin actuel : ", Verbose.REPLANIF.masque);
		while(iterCheminPrint.hasNext())
			log.debug(iterCheminPrint.getIndex()+" : "+iterCheminPrint.next(), Verbose.REPLANIF.masque);
		*/
	}
	
	private boolean isIndexValid(int index)
	{
		return !empty && minus(index, indexFirst) < minus(indexLast, indexFirst);
	}
	
	protected CinematiqueObs get(int index)
	{
		if(isIndexValid(index) && !empty)
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
		uptodate = true;
		indexLast = indexFirst;
		empty = true;
		
		if(graphic)
			updateAffichage();
	}
	
	/**
	 * Doit être mise à "true" si le trajet est entièrement planifié
	 * @param uptodate
	 */
	@Override
	public synchronized void setUptodate()
	{
		uptodate = true;

	}
	
	public boolean isUptodate()
	{
		return uptodate;
	}
	
	public int getCurrentIndex()
	{
		return indexFirst;
	}

	/**
	 * Mise à jour, depuis le bas niveau, de la cinématique actuelle
	 * Renvoie la cinématique actuelle
	 * @param indexTrajectory
	 */
	public synchronized Cinematique setCurrentIndex(int indexTrajectory)
	{
		indexFirst = indexTrajectory;
		if(empty)
			indexLast = indexFirst;
		if(graphic)
			updateAffichage();
		return chemin[indexFirst];
	}
	
	/**
	 * Renvoie l'arc du dernier point qu'on peut encore utiliser
	 * Un "checkColliding" doit être fait avant !
	 * @return
	 */
	@Override
	public Cinematique getLastValidCinematique()
	{
		if(!uptodate && needRestart && !empty)
		{
			needRestart = false; // la demande est partie
			return chemin[minus(indexLast,1)];
		}
		return null;
	}

	private void updateAffichage()
	{
		synchronized(buffer)
		{
			iterCheminPrint.reinit();
			Vec2RO last = null;
			
			for(int i = 0; i < 256; i++)
			{
				if(aff[i] != null)
				{
					buffer.removeSupprimable(aff[i]);
					aff[i] = null;
				}
				if(affSeg[i] != null)
				{
					buffer.removeSupprimable(affSeg[i]);
					affSeg[i] = null;
				}
			}
				
			while(iterCheminPrint.hasNext())
			{
				Cinematique a = iterCheminPrint.next();
				if(last != null)
				{
					affSeg[iterCheminPrint.getIndex()] = new Segment(last, a.getPosition(), Couleur.TRAJECTOIRE);
					buffer.addSupprimable(affSeg[iterCheminPrint.getIndex()]);
				}
				
				aff[iterCheminPrint.getIndex()] = new ObstacleCircular(a.getPosition(), 8, Couleur.TRAJECTOIRE);
				last = a.getPosition();
				buffer.addSupprimable(aff[iterCheminPrint.getIndex()]);
			}
			buffer.notify();
		}
	}
	
	/**
	 * Return indice1 - indice2
	 * @param indice1
	 * @param indice2
	 * @return
	 */
	public final int minus(int indice1, int indice2)
	{
		return (indice1 - indice2 + 256) & 0xFF;
	}
	
	/**
	 * Return indice1 + indice2
	 * @param indice1
	 * @param indice2
	 * @return
	 */
	public final int add(int indice1, int indice2)
	{
		return (indice1 + indice2) & 0xFF;
	}

	/**
	 * Utilisé pour les tests uniquement
	 * @return
	 */
	public double getLastOrientation()
	{
		return chemin[minus(indexLast,1)].orientationReelle;
	}

	/**
	 * Utilisé pour "followTrajectory" du robot chrono
	 * @return
	 */
	public Cinematique getLastCinematique()
	{
		return chemin[minus(indexLast,1)];
	}

	@Override
	public boolean isReplanif()
	{
		return !uptodate;
	}
	
}
