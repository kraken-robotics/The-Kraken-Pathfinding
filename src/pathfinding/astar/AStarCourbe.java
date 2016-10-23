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

package pathfinding.astar;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Stack;

import memory.NodeMM;
import memory.CinemObsMM;
import obstacles.types.ObstacleCircular;
import pathfinding.RealGameState;
import pathfinding.astar.arcs.ArcCourbe;
import pathfinding.astar.arcs.ArcManager;
import pathfinding.chemin.CheminPathfinding;
import pathfinding.dstarlite.DStarLite;
import pathfinding.dstarlite.gridspace.PointGridSpace;
import config.Config;
import config.ConfigInfo;
import config.Configurable;
import container.Service;
import exceptions.PathfindingException;
import graphic.PrintBuffer;
import graphic.printable.Layer;
import robot.Cinematique;
import robot.DirectionStrategy;
import robot.Speed;
import utils.Log;

/**
 * A* qui utilise le D* Lite comme heuristique pour fournir une trajectoire courbe
 * @author pf
 *
 */

public class AStarCourbe implements Service, Configurable
{
	protected DirectionStrategy directionstrategyactuelle;
	protected Log log;
	private ArcManager arcmanager;
	private DStarLite dstarlite;
	private RealGameState state;
	private NodeMM memorymanager;
	private PrintBuffer buffer;
	private Cinematique arrivee;
	private AStarCourbeNode depart;
	private CheminPathfinding chemin;
	private CinemObsMM cinemMemory;
	private boolean graphicTrajectory, graphicDStarLite;
	private int dureeMaxPF;
	private Speed vitesseMax;
	
	/**
	 * Comparateur de noeud utilisé par la priority queue
	 * @author pf
	 *
	 */
	private class AStarCourbeNodeComparator implements Comparator<AStarCourbeNode>
	{
		@Override
		public int compare(AStarCourbeNode arg0, AStarCourbeNode arg1)
		{
			// Ordre lexico : on compare d'abord first, puis second
			int tmp = (int) (arg0.f_score - arg1.f_score);
			if(tmp != 0)
				return tmp;
			return (int) (arg0.g_score - arg1.g_score);
		}
	}

	private final HashSet<AStarCourbeNode> closedset = new HashSet<AStarCourbeNode>();
	private final PriorityQueue<AStarCourbeNode> openset = new PriorityQueue<AStarCourbeNode>(PointGridSpace.NB_POINTS, new AStarCourbeNodeComparator());
	private Stack<ArcCourbe> pileTmp = new Stack<ArcCourbe>();
	
	/**
	 * Constructeur du AStarCourbe
	 */
	public AStarCourbe(Log log, DStarLite dstarlite, ArcManager arcmanager, RealGameState state, CheminPathfinding chemin, NodeMM memorymanager, CinemObsMM rectMemory, PrintBuffer buffer, AStarCourbeNode depart)
	{
		this.log = log;
		this.arcmanager = arcmanager;
		this.state = state;
		this.memorymanager = memorymanager;
		this.chemin = chemin;
		this.depart = depart;
		this.state = state;
		this.dstarlite = dstarlite;
		this.cinemMemory = rectMemory;
		this.buffer = buffer;
	}
	
	/**
	 * Le calcul du AStarCourbe
	 * @param depart
	 * @return
	 * @throws PathfindingException 
	 */
	protected final void process() throws PathfindingException
	{
		depart.parent = null;
		depart.cameFromArcCubique = null;
		depart.g_score = 0;
		Double heuristique;
		heuristique = dstarlite.heuristicCostCourbe((depart.state.robot).getCinematique());
		if(heuristique == null)
			throw new PathfindingException("DStarLiteException pour le point de départ !");
		else if(heuristique == Integer.MAX_VALUE)
			throw new PathfindingException("Le dstarlite dit qu'aucun chemin n'est possible…");

		depart.f_score = heuristique;
		openset.clear();
		openset.add(depart);	// Les nœuds à évaluer
		closedset.clear();
		
		long debutRecherche = System.currentTimeMillis();
		
		AStarCourbeNode current, successeur;
		do
		{
			current = openset.poll();
			
			if(closedset.contains(current)) // si on a déjà fait ce point ou un point très proche…
				continue;

			closedset.add(current);

			// ce calcul étant un peu lourd, on ne le fait que si le noeud a été choisi, et pas à la sélection des voisins (dans hasNext par exemple)
			if(!arcmanager.isReachable(current))
			{
//				log.debug("Collision");
				if(current.cameFromArcCubique != null)
				{
					cinemMemory.destroyNode(current.cameFromArcCubique);
					current.cameFromArcCubique = null;
				}
				memorymanager.destroyNode(current);
				continue; // collision mécanique attendue. On passe au suivant !
			}
			
			// affichage
			if(graphicTrajectory && current.getArc() != null)
				for(int i = 0; i < current.getArc().getNbPoints(); i++)
					buffer.addSupprimable(new ObstacleCircular(current.getArc().getPoint(i).getPosition(), 4), Layer.FOREGROUND);

/*			if(current.cameFromArc != null)
			{
				heuristique = dstarlite.heuristicCostCourbe((current.state.robot).getCinematique()) / current.cameFromArc.getVitesseTr();
				log.debug("Heuristique : "+heuristique+" ("+current.state.robot.getCinematique().getPosition().distance(arrivee.getPosition()) / current.cameFromArc.getVitesseTr()+")");
			}*/
						
			// Si on est arrivé, on reconstruit le chemin
			// On est arrivé seulement si on vient d'un arc cubique			
			if(current.cameFromArcCubique != null)
			{
//				log.debug("On est arrivé !");
				partialReconstruct(current);
				chemin.setUptodate(true);
				memorymanager.empty();
				cinemMemory.empty();
				return;
			}
			
			long elapsed = System.currentTimeMillis() - debutRecherche;
			if(elapsed > dureeMaxPF) // étant donné qu'il peut continuer jusqu'à l'infini...
			{
				memorymanager.empty();
				cinemMemory.empty();
				throw new PathfindingException("Timeout AStarCourbe !");
			}

			// On parcourt les voisins de current

			arcmanager.reinitIterator(current, directionstrategyactuelle);
			while(arcmanager.hasNext())
			{
				// On vérifie *très* régulièremet s'il ne faut pas fournir un chemin partiel
				if(chemin.needPartial())
				{
					log.debug("Reconstruction partielle demandée !");
					partialReconstruct(current);
					// Il est nécessaire de copier current dans depart car current
					// est effacé quand le memorymanager est vidé. Cette copie n'est effectuée qu'ici
					current.copyReconstruct(depart);
					memorymanager.empty();
					cinemMemory.empty();
					// En faisant "openset.clear()", il force le pathfinding a continuer sur sa lancée sans
					// remettre en cause la trajectoire déjà calculée
					openset.clear();
					openset.add(depart); // et on repart !
					break;
				}

				successeur = memorymanager.getNewNode();

				// S'il y a un problème, on passe au suivant (interpolation cubique impossible par exemple)
				if(!arcmanager.next(successeur, vitesseMax, arrivee))
				{
					if(successeur.cameFromArcCubique != null)
					{
						cinemMemory.destroyNode(successeur.cameFromArcCubique);
						successeur.cameFromArcCubique = null;
					}
					memorymanager.destroyNode(successeur);
					continue;
				}

				successeur.parent = current;
				successeur.g_score = current.g_score + arcmanager.distanceTo(successeur);
				
				heuristique = dstarlite.heuristicCostCourbe((successeur.state.robot).getCinematique());
				if(heuristique == null)
				{
					if(successeur.cameFromArcCubique != null)
					{
						cinemMemory.destroyNode(successeur.cameFromArcCubique);
						successeur.cameFromArcCubique = null;
					}
					memorymanager.destroyNode(successeur);
					continue;
				}
				
				successeur.f_score = successeur.g_score + heuristique / successeur.getArc().getVitesseTr();
								
				openset.add(successeur);
			}

		} while(!openset.isEmpty());
		
		/**
		 * Impossible car un nombre infini de nœuds !
		 */
		memorymanager.empty();
		cinemMemory.empty();
		throw new PathfindingException("Recherche AStarCourbe échouée");
	}
	
	/**
	 * Reconstruit le chemin. Il peut reconstruire le chemin même si celui-ci n'est pas fini.
	 * @param best
	 * @param last
	 */
	private final void partialReconstruct(AStarCourbeNode best)
	{
		AStarCourbeNode noeudParent = best;
		ArcCourbe arcParent = best.getArc();
		
		while(noeudParent.parent != null)
		{
			pileTmp.push(arcParent);
			noeudParent = noeudParent.parent;
			arcParent = noeudParent.getArc();
		}

		// chemin.add fait des copies des arcs
		while(!pileTmp.isEmpty())
			chemin.add(pileTmp.pop());
	}

	@Override
	public void useConfig(Config config)
	{
		graphicTrajectory = config.getBoolean(ConfigInfo.GRAPHIC_TRAJECTORY);
		graphicDStarLite = config.getBoolean(ConfigInfo.GRAPHIC_D_STAR_LITE_FINAL);
		dureeMaxPF = config.getInt(ConfigInfo.DUREE_MAX_RECHERCHE_PF);
	}
				
	/**
	 * Calcul d'un chemin à partir d'un certain état (state) et d'un point d'arrivée (endNode).
	 * Le boolean permet de signaler au pathfinding si on autorise ou non le shootage d'élément de jeu pas déjà pris.
	 * @param state
	 * @param endNode
	 * @param shoot_game_element
	 * @return
	 * @throws PathfindingException 
	 */
	public void computeNewPath(Cinematique arrivee, DirectionStrategy directionstrategy) throws PathfindingException
	{
		vitesseMax = Speed.STANDARD;
		this.directionstrategyactuelle = directionstrategy;
		this.arrivee = arrivee;
		depart.init();
		state.copyAStarCourbe(depart.state);

		dstarlite.computeNewPath(depart.state.robot.getCinematique().getPosition(), arrivee.getPosition());
		if(graphicDStarLite)
			dstarlite.itineraireBrut();

		process();
	}
	
	public synchronized void updatePath() throws PathfindingException
	{
		depart.init();
		synchronized(state)
		{
			state.copyAStarCourbe(depart.state);
		}
		depart.state.robot.setCinematique(chemin.getLastValidCinematique());
		vitesseMax = Speed.REPLANIF; // TODO
		process();
	}

}