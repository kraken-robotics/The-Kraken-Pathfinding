package pathfinding.astar;

import java.util.BitSet;
import java.util.PriorityQueue;

import pathfinding.GameState;
import permissions.ReadWrite;
import container.Service;
import exceptions.FinMatchException;
import exceptions.PathfindingException;
import exceptions.PathfindingRobotInObstacleException;
import robot.RobotChrono;
import utils.Config;
import utils.Log;

/**
 * A*, recherche de chemin utilisée dans la stratégie
 * @author pf
 *
 */

public class AStar implements Service
{
	/**
	 * Les méthodes publiques sont "synchronized".
	 * Cela signifique que si un AStar calcule une recherche de chemin pour un thread, l'autre thread devra attendre.
	 * Par contre, on peut faire un AStar stratégique et un AStar de pathfinding simultanément.
	 * Normalement, ce n'est pas utile car tous appels à AStar devraient être fait par le StrategyThread
	 */
	
	/**
	 * Les analogies pathfinding/stratégie sont:
	 * un noeud est un GameState<RobotChrono> dans les deux cas
	 * un arc est un Script pour l'arbre des possibles,
	 *   un SegmentTrajectoireCourbe pour le pathfinding
	 */
	
	private PriorityQueue<AStarNode> openset = new PriorityQueue<AStarNode>(GridSpaceStrategie.NB_POINTS, new AStarNodeComparator());	 // The set of tentative nodes to be evaluated

	private BitSet closedset = new BitSet(GridSpaceStrategie.NB_POINTS);

	private PathfindingException pathfindingexception;
	
	protected Log log;
	private int arrivee;
	private AStarArcManager arcmanager;
	private AStarMemoryManager memorymanager;
	private GridSpaceStrategie gridspace;
	
	/**
	 * Constructeur du AStar de pathfinding ou de stratégie, selon AM
	 */
	public AStar(Log log, AStarArcManager arcmanager, AStarMemoryManager memorymanager, GridSpaceStrategie gridspace)
	{
		this.log = log;
		this.arcmanager = arcmanager;
		this.memorymanager = memorymanager;
		this.gridspace = gridspace;
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
	public synchronized void computePath(GameState<RobotChrono,ReadWrite> depart, int arrivee, boolean shoot_game_element) throws PathfindingException, PathfindingRobotInObstacleException, FinMatchException
	{
		this.arrivee = arrivee;
//			GameState.setAvoidGameElement(depart, !shoot_game_element);

//			log.debug("Cherche un chemin entre "+pointDepart+" et "+endNode, this);
		
//			log.debug("Recherche de chemin entre "+pointDepart+" et "+indice_point_arrivee, this);
		process(depart);
	}
	
	/**
	 * Le calcul d'AStar à proprement parlé.
	 * A besoin de l'état de départ, de l'arcmanager.
	 * Si shouldReconstruct est vrai, alors on reconstruit le chemin même si on n'est pas arrivé
	 * à bon port. C'est le cas de la stratégie; par contre, un chemin incomplet n'est pas
	 * retourné avec le pathfinding.
	 * @param depart
	 * @param arcmanager
	 * @param shouldReconstruct
	 * @return
	 * @throws PathfindingException
	 * @throws MemoryManagerException
	 */
	private GameState<RobotChrono,ReadWrite> process(GameState<RobotChrono,ReadWrite> departState) throws PathfindingException
	{
		AStarNode depart = new AStarNode();
		depart.state = departState;
		closedset.clear();
		openset.clear();
		openset.add(depart);	// The set of tentative nodes to be evaluated, initially containing the start node
		depart.g_score = 0;
		depart.f_score = arcmanager.heuristicCost(depart);
		
		AStarNode current;

		while (!openset.isEmpty())
		{
			current = openset.poll();
			
			// élément déjà fait
			// cela parce qu'il y a des doublons dans openset
			if(closedset.get(current.hash))
			{
				memorymanager.destroyNode(current);
				continue;
			}
			
			// Si on est arrivé, on reconstruit le chemin
			if(current.hash == arrivee)
			{
				memorymanager.empty();
				return current.state;
			}

			closedset.set(current.hash);
		    
			// On parcourt les voisins de current.
			arcmanager.reinitIterator(current);

			boolean encoreUnSuccesseur = false;
			try {
				encoreUnSuccesseur = arcmanager.hasNext();
			} catch (FinMatchException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			/** Puisque current ne pourra pas être réutilisé,
			 * il faut le détruire
			 */
			if(!encoreUnSuccesseur)
				memorymanager.destroyNode(current);
			
			boolean reuseOldSuccesseur = false;
			AStarNode successeur = null;
			
			while(encoreUnSuccesseur)
			{
				int voisin = arcmanager.next();
				try {
					encoreUnSuccesseur = arcmanager.hasNext();
				} catch (FinMatchException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				
				try {
					if(!encoreUnSuccesseur) // le dernier successeur
					{
						if(reuseOldSuccesseur)
							memorymanager.destroyNode(successeur);
						successeur = current;
					}
					else
					{
						if(!reuseOldSuccesseur)
							successeur = memorymanager.getNewNode();
						current.copy(successeur);
					}
				} catch (FinMatchException e1) {
					// ne devrait pas arriver!
					throw new PathfindingException();
				}
				reuseOldSuccesseur = false;
				
				// successeur est modifié lors du "distanceTo"
				// si ce successeur dépasse la date limite, on l'annule
				int tentative_g_score;
				try {
					tentative_g_score = current.g_score + arcmanager.distanceTo(successeur, voisin);
					if(tentative_g_score - current.g_score < 0)
						log.critical("Distance négative! "+(tentative_g_score - current.g_score));
//					log.debug("Tentative pour "+voisin+": "+tentative_g_score, this);
				} catch (FinMatchException e) {
					if(encoreUnSuccesseur)
						reuseOldSuccesseur = true;
					else
						memorymanager.destroyNode(successeur);
					continue;
				}

				if(closedset.get(successeur.hash)) // si closedset contient ce hash
				{
					if(encoreUnSuccesseur)
						reuseOldSuccesseur = true;
					else
						memorymanager.destroyNode(successeur);
					continue;
				}
				
				if(tentative_g_score < successeur.g_score)
				{
					successeur.g_score = tentative_g_score;
					successeur.f_score = tentative_g_score + arcmanager.heuristicCost(successeur);
					openset.add(successeur);
					// reuseOldSuccesseur reste à false
				}
				else
				{
					if(encoreUnSuccesseur)
						reuseOldSuccesseur = true;
					else
						memorymanager.destroyNode(successeur);
				}
			}
			
		}
		
		// Pathfinding terminé sans avoir atteint l'arrivée
		memorymanager.empty();
		
		// La stratégie renvoie un chemin partiel, pas le pathfinding qui lève une exception
		if(pathfindingexception == null)
			pathfindingexception = new PathfindingException();
		throw pathfindingexception;
		
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
}
