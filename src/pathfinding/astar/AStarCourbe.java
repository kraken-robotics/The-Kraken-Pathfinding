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
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Stack;

import memory.NodeMM;
import memory.CinemObsMM;
import obstacles.types.ObstacleCircular;
import pathfinding.DirectionStrategy;
import pathfinding.RealGameState;
import pathfinding.SensFinal;
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
import graphic.printable.Couleur;
import robot.Cinematique;
import robot.CinematiqueObs;
import robot.Speed;
import utils.Log;

/**
 * A* qui utilise le D* Lite comme heuristique pour fournir une trajectoire courbe
 * @author pf
 *
 */

public class AStarCourbe implements Service, Configurable
{
	protected SensFinal sens;
	protected Log log;
	private ArcManager arcmanager;
	private DStarLite dstarlite;
	private RealGameState state;
	private NodeMM memorymanager;
	private PrintBuffer buffer;
	private AStarCourbeNode depart;
	private AStarCourbeNode trajetDeSecours;
	private CheminPathfinding chemin;
	private CinemObsMM cinemMemory;
	private boolean graphicTrajectory, graphicDStarLite, graphicTrajectoryAll;
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
	private LinkedList<CinematiqueObs> trajectory = new LinkedList<CinematiqueObs>();

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
	protected final void process(boolean shoot) throws PathfindingException
	{
		trajetDeSecours = null;
		depart.parent = null;
		depart.cameFromArcDynamique = null;
		depart.g_score = 0;
		Double heuristique;
		heuristique = arcmanager.heuristicCostCourbe((depart.state.robot).getCinematique(), sens);

		// Il faut bien mettre quelque chose…
		if(heuristique == null)
			heuristique = arcmanager.heuristicDirect(depart.state.robot.getCinematique());
		
		else if(heuristique == Integer.MAX_VALUE)
			throw new PathfindingException("Le dstarlite dit qu'aucun chemin n'est possible…");

		depart.f_score = heuristique / Speed.STANDARD.translationalSpeed;
		openset.clear();
		openset.add(depart);	// Les nœuds à évaluer
		closedset.clear();
		
		long debutRecherche = System.currentTimeMillis();
		
		AStarCourbeNode current, successeur;
		do
		{
			current = openset.poll();
			
			// si on a déjà fait ce point ou un point très proche…
			// exception si c'est un point d'arrivée
			if(!closedset.add(current) && !arcmanager.isArrived(current))
			{
				if(graphicTrajectory || graphicTrajectoryAll)
					current.setDead();
				continue;
			}

			// ce calcul étant un peu lourd, on ne le fait que si le noeud a été choisi, et pas à la sélection des voisins (dans hasNext par exemple)
			if(!arcmanager.isReachable(current, shoot))
			{
				destroy(current);
				continue; // collision mécanique attendue. On passe au suivant !
			}
			
			// affichage
			if(graphicTrajectory && !graphicTrajectoryAll)
				buffer.addSupprimable(current);

			// Si on est arrivé, on reconstruit le chemin
			// On est arrivé seulement si on vient d'un arc cubique
			if(current == trajetDeSecours)
			{
//				log.debug("On est arrivé !");
				chemin.setUptodate(true);
				partialReconstruct(current);
				memorymanager.empty();
				cinemMemory.empty();
				return;
			}
			
			long elapsed = System.currentTimeMillis() - debutRecherche;
			if(elapsed > dureeMaxPF) // étant donné qu'il peut continuer jusqu'à l'infini...
			{
				memorymanager.empty();
				cinemMemory.empty();
				if(trajetDeSecours != null) // si on a un trajet de secours, on l'utilise
				{
					chemin.setUptodate(true);
					partialReconstruct(trajetDeSecours);
					return;
				}
				throw new PathfindingException("Timeout AStarCourbe !");
			}

			// On parcourt les voisins de current

			arcmanager.reinitIterator(current);
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
					closedset.clear();
					// En faisant "openset.clear()", il force le pathfinding a continuer sur sa lancée sans
					// remettre en cause la trajectoire déjà calculée
					openset.clear();
					openset.add(depart); // et on repart !
					break;
				}

				successeur = memorymanager.getNewNode();
				successeur.cameFromArcDynamique = null;

				// S'il y a un problème, on passe au suivant (interpolation cubique impossible par exemple)
				if(!arcmanager.next(successeur, vitesseMax))
				{
					destroy(successeur);
					continue;
				}
				
				successeur.parent = current;
				successeur.g_score = current.g_score + arcmanager.distanceTo(successeur);
			
				// on a déjà visité un point proche?
				if(closedset.contains(successeur))
				{
					destroy(successeur);
					continue;
				}
				
				// affichage
				if(graphicTrajectoryAll)
					buffer.addSupprimable(successeur);
				
				heuristique = arcmanager.heuristicCostCourbe(successeur.state.robot.getCinematique(), sens);
				if(heuristique == null)
					heuristique = arcmanager.heuristicDirect(successeur.state.robot.getCinematique());
				
				successeur.f_score = successeur.g_score + heuristique / successeur.getArc().getVitesseTr();
				
				// noeud d'arrivé
				if(arcmanager.isArrived(successeur) && arcmanager.isReachable(successeur, shoot)
					&& (trajetDeSecours == null || trajetDeSecours.f_score > successeur.f_score))
				{
//					log.debug("Arrivée trouvée !");
					trajetDeSecours = successeur;
				}
				
				openset.add(successeur);
			}

		} while(!openset.isEmpty());
		
		/**
		 * Plus aucun nœud à explorer
		 */
		memorymanager.empty();
		cinemMemory.empty();
		throw new PathfindingException("Recherche AStarCourbe échouée");
	}
 	
	private void destroy(AStarCourbeNode n)
	{
		if(n.cameFromArcDynamique != null)
			cinemMemory.destroyNode(n.cameFromArcDynamique);
		memorymanager.destroyNode(n);
	}
	
	/**
	 * Reconstruit le chemin. Il peut reconstruire le chemin même si celui-ci n'est pas fini.
	 * @param best
	 * @param last
	 * @throws PathfindingException 
	 */
	private final void partialReconstruct(AStarCourbeNode best) throws PathfindingException
	{
		AStarCourbeNode noeudParent = best;
		ArcCourbe arcParent = best.getArc();
		
		while(noeudParent.parent != null)
		{
			pileTmp.push(arcParent);
			noeudParent = noeudParent.parent;
			arcParent = noeudParent.getArc();
		}
		trajectory.clear();

		// chemin.add fait des copies des points
		while(!pileTmp.isEmpty())
		{
			ArcCourbe a = pileTmp.pop();
			for(int i = 0; i < a.getNbPoints(); i++)
				trajectory.add(a.getPoint(i));
		}
		chemin.add(trajectory);
	}

	@Override
	public void useConfig(Config config)
	{
		graphicTrajectory = config.getBoolean(ConfigInfo.GRAPHIC_TRAJECTORY);
		graphicTrajectoryAll = config.getBoolean(ConfigInfo.GRAPHIC_TRAJECTORY_ALL);
		graphicDStarLite = config.getBoolean(ConfigInfo.GRAPHIC_D_STAR_LITE_FINAL);
		dureeMaxPF = config.getInt(ConfigInfo.DUREE_MAX_RECHERCHE_PF);
	}
				
	/**
	 * Valeurs par défaut : DirectionStrategy.FASTEST et SensFinal.AUCUNE_PREF
	 * @param arrivee
	 * @param shoot
	 * @throws PathfindingException
	 */
	public void computeNewPath(Cinematique arrivee, boolean shoot) throws PathfindingException
	{
		computeNewPath(arrivee, SensFinal.AUCUNE_PREF, shoot);
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
	public void computeNewPath(Cinematique arrivee, SensFinal sens, boolean shoot) throws PathfindingException
	{
		vitesseMax = Speed.STANDARD;
		this.sens = sens;
		depart.init();
		state.copyAStarCourbe(depart.state);
		arcmanager.configureArcManager(sens, DirectionStrategy.defaultStrategy, arrivee);

		dstarlite.computeNewPath(depart.state.robot.getCinematique().getPosition(), arrivee.getPosition());
		if(graphicDStarLite)
			dstarlite.itineraireBrut();

		process(shoot);
	}
	
	/**
	 * Replanification. On conserve la même DirectionStrategy ainsi que le même SensFinal
	 * Par contre, si besoin est, on peut changer la politique de shootage d'éléments de jeu
	 * @param shoot
	 * @throws PathfindingException
	 */
	public synchronized void updatePath(boolean shoot) throws PathfindingException
	{
		depart.init();
		synchronized(state)
		{
			state.copyAStarCourbe(depart.state);
		}

		closedset.clear();
		depart.state.robot.setCinematique(chemin.getLastValidCinematique());
		
		// On met à jour le D* Lite
		dstarlite.updateStart(depart.state.robot.getCinematique().getPosition());
		dstarlite.updateObstacles();
		if(graphicDStarLite)
			dstarlite.itineraireBrut();

		vitesseMax = Speed.REPLANIF;
		process(shoot);
	}

}