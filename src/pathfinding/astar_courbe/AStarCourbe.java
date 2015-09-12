package pathfinding.astar_courbe;

import java.util.PriorityQueue;

import pathfinding.GameState;
import pathfinding.dstarlite.DStarLite;
import pathfinding.dstarlite.GridSpace;
import pathfinding.thetastar.CheminPathfinding;
import permissions.ReadOnly;
import container.Service;
import exceptions.FinMatchException;
import exceptions.PathfindingException;
import robot.DirectionStrategy;
import robot.RobotReal;
import tests.graphicLib.Fenetre;
import utils.Config;
import utils.Log;
import utils.Vec2;

/**
 * Theta*, qui lisse le résultat du D* Lite et fournit une trajectoire courbe
 * @author pf
 *
 */

public class AStarCourbe implements Service
{
	private PriorityQueue<AStarCourbeNode> openset = new PriorityQueue<AStarCourbeNode>(GridSpace.NB_POINTS, new AStarCourbeNodeComparator());	 // The set of tentative nodes to be evaluated
	private DirectionStrategy directionstrategyactuelle;
	
	protected Log log;
	private DStarLite dstarlite;
	private AStarCourbeArcManager arcmanager;
	private GameState<RobotReal,ReadOnly> state;
	private CheminPathfinding cheminContainer;
	private AStarCourbeMemoryManager memorymanager;
	private Fenetre fenetre;
	private Vec2<ReadOnly> arrivee;

	/**
	 * Constructeur du AStarCourbe
	 */
	public AStarCourbe(Log log, DStarLite dstarlite, AStarCourbeArcManager arcmanager, GameState<RobotReal,ReadOnly> state, CheminPathfinding chemin, AStarCourbeMemoryManager memorymanager)
	{
		this.log = log;
		this.dstarlite = dstarlite;
		this.arcmanager = arcmanager;
		this.state = state;
		this.cheminContainer = chemin;
		this.memorymanager = memorymanager;

		if(Config.graphicAStarCourbe)
			fenetre = Fenetre.getInstance();
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
	public synchronized void computeNewPath(Vec2<ReadOnly> arrivee, boolean ejecteGameElement, DirectionStrategy directionstrategy) throws PathfindingException
	{
//		if(Config.graphicAStarCourbe)
//			fenetre.setColor(arrivee, Fenetre.Couleur.VIOLET);

		this.directionstrategyactuelle = directionstrategy;
		arcmanager.setEjecteGameElement(ejecteGameElement);
		
		AStarCourbeNode depart;
		synchronized(state)
		{
			depart = memorymanager.getNewNode();
			depart.init();
			depart.first = true;
			GameState.copyAStarCourbe(state, depart.state);
		}
		
		dstarlite.computeNewPath(depart.state.robot.getPosition(), arrivee);
		process(depart);
		
		if(Config.graphicAStarCourbe)
			printChemin();
	}
	
	private void printChemin()
	{
//		ArcCourbe[] cheminAff = cheminContainer.get();	
//		for(ArcCourbe arc : cheminAff)
//			fenetre.setColor(arc.getGridpointArrivee(), Fenetre.Couleur.VIOLET);
	}
	
	public synchronized void updatePath() throws PathfindingException
	{

		AStarCourbeNode depart;
		synchronized(state)
		{
			depart = memorymanager.getNewNode();
			depart.init();
			depart.first = true;
			GameState.copyAStarCourbe(state, depart.state);
		}
		
		dstarlite.updatePath(depart.state.robot.getPosition());
		process(depart);
		
		if(Config.graphicAStarCourbe)
			printChemin();
	}
		
	/**
	 * Le calcul du AStarCourbe
	 * @param depart
	 * @return
	 */
	private void process(AStarCourbeNode depart)
	{
		depart.came_from = null;
		depart.came_from_arc = null;
		depart.g_score = 0;
		depart.f_score = arcmanager.heuristicCostAStarCourbe(depart);

		cheminContainer.notUptodate();
		memorymanager.empty();

		openset.clear();
		openset.add(depart);	// The set of tentative nodes to be evaluated, initially containing the start node

		AStarCourbeNode current, successeur;

		do
		{
			current = openset.poll();
			
//			log.debug(current);
			
			// Si on est arrivé, on reconstruit le chemin
			if(current.state.robot.getPosition().squaredDistance(arrivee) < 2500)
			{
//				log.debug("On est arrivé !");
				partialReconstruct(current);
				return;
			}
			
			// On parcourt les voisins de current et de son prédecesseur.

			arcmanager.reinitIterator(current, directionstrategyactuelle);
			
			while(arcmanager.hasNext())
			{
				if(cheminContainer.isNeededToStartAgain())
				{
					partialReconstruct(current);
					openset.clear();
					openset.add(current);
					break;
				}

				successeur = arcmanager.next();
				
				GameState.copyAStarCourbe(current.state.getReadOnly(), successeur.state);

//				successeur.came_from = current; //TODO fait par l'arc manager
				try {
					successeur.g_score = current.g_score + arcmanager.distanceTo(successeur);
				} catch (FinMatchException e) {
					continue;
				}
				successeur.f_score = successeur.g_score + arcmanager.heuristicCostAStarCourbe(successeur);
				/**
				 * Normalement, il suffit de comparer les g_scores.
				 * En effet, l'heuristique d'un point est censé être unique, et alors comparer f1 à
				 * f2 revient à comparer f1 - h = g1 à f2 - h = g2.
				 * Or, c'est ici l'heuristique dépend de l'orientation du robot, et donc n'est pas unique pour un gridpoint.
				 * Il est donc nécessaire de comparer les deux f_scores
				 */
				openset.add(successeur);
				
//				if(Config.graphicAStarCourbe)
//					fenetre.setColor(current.gridpoint, Fenetre.Couleur.ROUGE);

			}
			
		} while(!openset.isEmpty());
		
		/**
		 * Impossible car DStarLite a déjà trouvé un chemin.
		 */
		log.critical("AStarCourbe n'a pas trouvé de chemin !");
		return;
	}
	
	private void partialReconstruct(AStarCourbeNode best)
	{
		synchronized(cheminContainer)
		{
			int k = 0;
			ArcCourbe[] chemin = cheminContainer.get();
			AStarCourbeNode noeud_parent = best;
			ArcCourbe arc_parent = best.came_from_arc;
			while(!best.first)
			{
	//			log.debug(arc_parent);
				arc_parent.copy(chemin[k]);
				k++;
				noeud_parent = noeud_parent.came_from;
				arc_parent = noeud_parent.came_from_arc;
			}
			cheminContainer.setDernierIndiceChemin(k-1);
		}
		openset.clear();
		openset.add(best);
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
		
}