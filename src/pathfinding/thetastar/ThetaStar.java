package pathfinding.thetastar;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;

import pathfinding.dstarlite.DStarLite;
import pathfinding.dstarlite.GridSpace;
import permissions.ReadOnly;
import permissions.ReadWrite;
import container.Service;
import exceptions.FinMatchException;
import exceptions.PathfindingException;
import exceptions.PathfindingRobotInObstacleException;
import robot.RobotChrono;
import robot.RobotReal;
import strategie.GameState;
import utils.Config;
import utils.Log;
import utils.Vec2;

/**
 * TODO Optimisation:
 * - ne pas utiliser tous les noeuds au début, le faire quand on en a besoin
 * - super-structure qui convient le hash et le gamestate?
 */

/**
 * Theta*, qui lisse le résultat du D* Lite et fournit une trajectoire courbe
 * @author pf
 *
 */

public class ThetaStar implements Service
{
	private LinkedList<ThetaStarNode> openset = new LinkedList<ThetaStarNode>();	 // The set of tentative nodes to be evaluated
	private BitSet closedset = new BitSet(GridSpace.NB_POINTS);
	
	private ArrayList<LocomotionArc> cheminTmp = new ArrayList<LocomotionArc>();
	private ThetaStarNode[] memory = new ThetaStarNode[GridSpace.NB_POINTS];
	
	protected Log log;
	private DStarLite dstarlite;
	private ArcManager arcmanager;
	private GameState<RobotReal,ReadOnly> state;
	private CheminPathfinding chemin;
	private GameState<RobotChrono,ReadWrite> stateSuccesseur;
	private GameState<RobotChrono,ReadWrite> last;
	private long nbPF = 0;

	/**
	 * Constructeur du ThetaStar
	 */
	public ThetaStar(Log log, DStarLite dstarlite, ArcManager arcmanager, GameState<RobotReal,ReadOnly> state, CheminPathfinding chemin)
	{
		this.log = log;
		this.dstarlite = dstarlite;
		this.arcmanager = arcmanager;
		this.state = state;
		this.chemin = chemin;
		last = GameState.cloneGameState(state);
		stateSuccesseur = GameState.cloneGameState(state);
		for(int i = 0; i < GridSpace.NB_POINTS; i++)
		{
			memory[i] = new ThetaStarNode(GameState.cloneGameState(state), i);
		}
	}

	/**
	 * Calcul d'un chemin à partir d'un certain état (state) et d'un point d'arrivée (endNode).
	 * Le boolean permet de signaler au pathfinding si on autorise ou non le shootage d'élément de jeu pas déjà pris.
	 * @param state
	 * @param endNode
	 * @param shoot_game_element
	 * @return
	 * @throws PathfindingException
	 * @throws PathfindingRobotInObstacleException
	 * @throws FinMatchException
	 * @throws MemoryManagerException
	 */
	public void computeNewPath(Vec2<ReadOnly> arrivee, boolean shoot_game_element) throws PathfindingException
	{
		ThetaStarNode depart;
		GameState.copy(state, last);
		dstarlite.computeNewPath(last.robot.getPosition(), arrivee);
		do {
			depart = memory[last.robot.getPositionGridSpace()];
			depart.init();
			GameState.copy(last.getReadOnly(), depart.state);
		}
		while(!process(depart));
	}
	
	public void updatePath() throws PathfindingException
	{
		ThetaStarNode depart;
		GameState.copy(state, last);
		dstarlite.updatePath(last.robot.getPosition());
		do {
			depart = memory[last.robot.getPositionGridSpace()];
			depart.init();
			GameState.copy(last.getReadOnly(), depart.state);
		}
		while(!process(depart));
	}
	
	/**
	 * Le calcul du ThetaStar
	 * @param depart
	 * @param arcmanager
	 * @param shouldReconstruct
	 * @return 
	 * @return
	 * @throws PathfindingException
	 * @throws MemoryManagerException
	 */
	private synchronized boolean process(ThetaStarNode depart) throws PathfindingException
	{
		nbPF++;
		int hashArrivee = dstarlite.getHashArrivee();

		openset.clear();
		closedset.clear();
		addToOpenset(depart);	// The set of tentative nodes to be evaluated, initially containing the start node
		depart.g_score = 0;	// Cost from start along best known path.
		// Estimated total cost from start to goal through y.
		depart.f_score = arcmanager.heuristicCostThetaStar(depart);
		
		ThetaStarNode current;

		while(!openset.isEmpty())
		{
			current = openset.pop();

			closedset.set(current.hash);
						
			if(reconstruct(current))
				return false;

			// Si on est arrivé, on reconstruit le chemin
			if(current.hash == hashArrivee)
				return true;
			
			// On parcourt les voisins de current et de son prédecesseur.
			arcmanager.reinitIterator(current.came_from, current);
			ThetaStarNode current_sauv = current;
			
			while(arcmanager.hasNext())
			{
				LocomotionArc voisin = arcmanager.nextProposition();
				if(closedset.get(voisin.gridpointArrivee) || !arcmanager.nextAccepted())
					continue;
				
				if(arcmanager.isFromPredecesseur())
					current = current_sauv.came_from;
				else
					current = current_sauv;

				GameState.copy(current.state.getReadOnly(), stateSuccesseur);

				// stateSuccesseur est modifié lors du "distanceTo"
				int tentative_g_score = current.g_score + arcmanager.distanceTo(stateSuccesseur, voisin);

				ThetaStarNode successeur = getFromMemory(stateSuccesseur.robot.getPositionGridSpace());
				
				if(tentative_g_score < successeur.g_score)
				{
					GameState.copy(stateSuccesseur.getReadOnly(), successeur.state);
					successeur.came_from = current;
					successeur.came_from_arc = voisin;
					successeur.g_score = tentative_g_score;
					successeur.f_score = tentative_g_score + arcmanager.heuristicCostThetaStar(successeur);
					openset.remove(successeur);
					addToOpenset(successeur);
				}
			}
			
		}
		
		// Pathfinding terminé sans avoir atteint l'arrivée
		return true;
		
	}
	
	private void addToOpenset(ThetaStarNode node)
	{
		Iterator<ThetaStarNode> iterator = openset.listIterator();
		int i = 0;
		while(iterator.hasNext())
		{
			if(node.f_score < iterator.next().f_score)
			{
				openset.add(i, node);
				return;
			}
			i++;
		}
		openset.add(node);
	}

	private boolean reconstruct(ThetaStarNode best)
	{
		if(chemin.needToStartAgain())
			return true;
		cheminTmp.clear();
		ThetaStarNode noeud_parent = best;
		LocomotionArc arc_parent = best.came_from_arc;
		while(arc_parent != null)
		{
			cheminTmp.add(0, arc_parent);
			noeud_parent = noeud_parent.came_from;
			arc_parent = noeud_parent.came_from_arc;
		}			
		chemin.set(cheminTmp);
		GameState.copy(best.state.getReadOnly(), last);
		return false;
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
	private ThetaStarNode getFromMemory(int hash)
	{
		ThetaStarNode out = memory[hash];
		
		/**
		 * Si ce point n'a pas encore été utilisé pour ce pathfinding, on l'initialise
		 */
		if(out.nbPF != nbPF)
		{
			out.init();
			out.nbPF = nbPF;
		}
		return out;
	}
		
}