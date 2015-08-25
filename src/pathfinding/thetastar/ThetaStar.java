package pathfinding.thetastar;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.PriorityQueue;

import pathfinding.dstarlite.DStarLite;
import pathfinding.dstarlite.GridSpace;
import permissions.ReadOnly;
import permissions.ReadWrite;
import container.Service;
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
	private PriorityQueue<ThetaStarNode> openset = new PriorityQueue<ThetaStarNode>(GridSpace.NB_POINTS, new ThetaStarNodeComparator());	 // The set of tentative nodes to be evaluated
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
			memory[i] = new ThetaStarNode(GameState.cloneGameState(state), i);
	}

	/**
	 * Calcul d'un chemin à partir d'un certain état (state) et d'un point d'arrivée (endNode).
	 * Le boolean permet de signaler au pathfinding si on autorise ou non le shootage d'élément de jeu pas déjà pris.
	 * @param state
	 * @param endNode
	 * @param shoot_game_element
	 * @return
	 */
	public synchronized void computeNewPath(Vec2<ReadOnly> arrivee, boolean shootGameElement)
	{
		ThetaStarNode depart;
		GameState.copy(state, last);
		arcmanager.setShootGameElement(shootGameElement);
		dstarlite.computeNewPath(last.robot.getPosition(), arrivee);
		do {
			depart = memory[last.robot.getPositionGridSpace()];
			depart.init();
			GameState.copy(last.getReadOnly(), depart.state);
		}
		while(!process(depart));
	}
	
	public synchronized void updatePath()
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
	 */
	private boolean process(ThetaStarNode depart)
	{
		nbPF++;
		int hashArrivee = dstarlite.getHashArrivee();

		openset.clear();
		closedset.clear();

		depart.nbPF = nbPF;
		depart.g_score = 0;	// Cost from start along best known path.
		// Estimated total cost from start to goal through y.
		depart.f_score = arcmanager.heuristicCostThetaStar(depart.state.getReadOnly());

		openset.add(depart);	// The set of tentative nodes to be evaluated, initially containing the start node

		ThetaStarNode current;

		while(!openset.isEmpty())
		{
			current = openset.poll();

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
				int tentative_f_score = tentative_g_score + arcmanager.heuristicCostThetaStar(stateSuccesseur.getReadOnly());
				
				ThetaStarNode successeur = getFromMemory(stateSuccesseur.robot.getPositionGridSpace());
				
				/**
				 * Normalement, il suffit de comparer les g_scores.
				 * En effet, l'heuristique d'un point est censé être unique, et alors comparer g1 + f à
				 * g2 + f revient à comparer g1 à g2.
				 * Or, c'est ici l'heuristique dépend de l'orientation du robot, et donc n'est pas unique pour un gridpoint.
				 * Il est donc nécessaire de comparer les deux f_scores
				 */
				if(tentative_f_score < successeur.f_score)
				{
					GameState.copy(stateSuccesseur.getReadOnly(), successeur.state);
					successeur.came_from = current;
					successeur.came_from_arc = voisin;
					successeur.g_score = tentative_g_score;
					successeur.f_score = tentative_f_score;
					if(openset.contains(successeur))
						openset.remove(successeur);
					openset.add(successeur);
				}
			}
			
		}
		
		// Pathfinding terminé sans avoir atteint l'arrivée.
		return true;
		
	}
	
/*	private void addToOpenset(ThetaStarNode node)
	{
		Iterator<ThetaStarNode> iterator = openset.listIterator();
		int i = 0;
		while(iterator.hasNext())
		{
			ThetaStarNode nodeList = iterator.next();
			// Contrairement à DStarLite qui explore près du point de départ, ThetaStar, en cas d'égalité, poursuit son chemin
			if(node.f_score < nodeList.f_score || (node.f_score == nodeList.f_score && node.g_score > nodeList.g_score))
			{
				openset.add(i, node);
				return;
			}
			i++;
		}
		openset.add(node);
	}
*/
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