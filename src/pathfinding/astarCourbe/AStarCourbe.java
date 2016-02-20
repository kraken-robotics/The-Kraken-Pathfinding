package pathfinding.astarCourbe;

import java.util.PriorityQueue;

import pathfinding.CheminPathfinding;
import pathfinding.GameState;
import pathfinding.dstarlite.DStarLite;
import pathfinding.dstarlite.GridSpace;
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
 * AStar* simplifié, qui lisse le résultat du D* Lite et fournit une trajectoire courbe
 * On suppose qu'il n'y a jamais collision de noeuds
 * (je parle de collision dans le sens "égalité", pas "robot qui fonce dans le mur"…)
 * @author pf
 *
 */

public class AStarCourbe implements Service
{
	private PriorityQueue<AStarCourbeNode> openset = new PriorityQueue<AStarCourbeNode>(GridSpace.NB_POINTS, new AStarCourbeNodeComparator());
	private DirectionStrategy directionstrategyactuelle;
	
	protected Log log;
	private DStarLite dstarlite;
	private AStarCourbeArcManager arcmanager;
	private GameState<RobotReal,ReadOnly> state;
	private CheminPathfinding cheminContainer;
	private AStarCourbeMemoryManager memorymanager;
	protected Fenetre fenetre;
	private Vec2<ReadOnly> arrivee;
	private AStarCourbeNode depart;

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
		depart = new AStarCourbeNode();

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
		
		depart.init();
		GameState.copyAStarCourbe(state, depart.state);
		
		dstarlite.computeNewPath(depart.state.robot.getPosition(), arrivee);
		process();
		
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
		synchronized(state)
		{
			depart.init();
			GameState.copyAStarCourbe(state, depart.state);
		}
		
		dstarlite.updatePath(depart.state.robot.getPosition());
		process();
		
		if(Config.graphicAStarCourbe)
			printChemin();
	}

	/**
	 * Le calcul du AStarCourbe
	 * @param depart
	 * @return
	 */
	private void process()
	{
		depart.came_from = null;
		depart.g_score = 0;
		depart.f_score = arcmanager.heuristicCost(depart);

		cheminContainer.resetChemin();
		memorymanager.empty();

		openset.clear();
		openset.add(depart);	// Les nœuds à évaluer

		AStarCourbeNode current, successeur;

		do
		{
			current = openset.poll();
			try {
				if(!arcmanager.execute(current))
					continue; // collision mécanique attendue. On passe au suivant !
			} catch (FinMatchException e) {
				continue;
			}
			
			// Si on est arrivé, on reconstruit le chemin
			if(current.state.robot.getPosition().squaredDistance(arrivee) < 50*50)
			{
				partialReconstruct(current, true);
				memorymanager.empty();
				return;
			}
			
			// On parcourt les voisins de current

			arcmanager.reinitIterator(current, directionstrategyactuelle);
			
			while(arcmanager.hasNext())
			{
				if(cheminContainer.doitFixerCheminPartiel())
				{
					partialReconstruct(current, false);
					// Il est nécessaire de copier current dans depart car current
					// est effacé quand le memorymanager est vidé. Cette copie n'est effectuée qu'ici
					current.copyReconstruct(depart);
					memorymanager.empty();
					openset.clear();
					openset.add(depart);
					break;
				}

				successeur = memorymanager.getNewNode();
				arcmanager.next(successeur);
				 
				successeur.g_score = current.g_score + arcmanager.distanceTo(successeur);
				successeur.f_score = successeur.g_score + arcmanager.heuristicCost(successeur);

				openset.add(successeur);
				
//				if(Config.graphicAStarCourbe)
//					fenetre.setColor(current.gridpoint, Fenetre.Couleur.ROUGE);

			}
			
		} while(!openset.isEmpty());
		
		/**
		 * Impossible car un nombre infini de nœuds !
		 */
		log.critical("AStarCourbe n'a pas trouvé de chemin !");
		return;
	}
	
	/**
	 * Reconstruit le chemin. Il peut reconstruire le chemin même si celui-ci n'est pas fini.
	 * En effet, en faisant "openset.clear()", il force le pathfinding a continuer sur sa lancée sans
	 * remettre en cause la trajectoire déjà calculée
	 * @param best
	 * @param last
	 */
	private void partialReconstruct(AStarCourbeNode best, boolean last)
	{
		synchronized(cheminContainer)
		{
			AStarCourbeNode noeud_parent = best;
			ArcCourbe arc_parent = best.came_from_arc;
			while(best.came_from != null)
			{
				cheminContainer.addArc(arc_parent);
				noeud_parent = noeud_parent.came_from;
				arc_parent = noeud_parent.came_from_arc;
			}
			cheminContainer.setFinish(last);
			cheminContainer.notify(); // on prévient le thread d'évitement qu'un chemin est disponible
		}
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
		
}