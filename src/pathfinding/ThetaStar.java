package pathfinding;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.BitSet;

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
	private static final int nb_max_element = 100; // le nombre d'élément différent de l'arbre qu'on parcourt. A priori, 100 paraît suffisant.
	
	private LinkedList<GameState<RobotChrono,ReadWrite>> openset = new LinkedList<GameState<RobotChrono,ReadWrite>>();	 // The set of tentative nodes to be evaluated
	private BitSet closedset = new BitSet(nb_max_element);
	private int[] came_from = new int[nb_max_element];
	private LocomotionArc[] came_from_arc = new LocomotionArc[nb_max_element];
	private int[] g_score = new int[nb_max_element];
	private int[] f_score = new int[nb_max_element];

	private ArrayList<LocomotionArc> cheminTmp = new ArrayList<LocomotionArc>();

	protected Log log;
	private DStarLite dstarlite;
	private MemoryManager memorymanager;
	private ArcManager arcmanager;
	private GameState<RobotReal,ReadOnly> state;
	private CheminPathfinding chemin;
	
	private GameState<RobotChrono,ReadWrite> last;

	/**
	 * Constructeur du ThetaStar
	 */
	public ThetaStar(Log log, DStarLite dstarlite, ArcManager arcmanager, MemoryManager memorymanager, GameState<RobotReal,ReadOnly> state, CheminPathfinding chemin)
	{
		this.log = log;
		this.dstarlite = dstarlite;
		this.arcmanager = arcmanager;
		this.memorymanager = memorymanager;
		this.state = state;
		this.chemin = chemin;
		GameState.copy(state, last);
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
		GameState<RobotChrono,ReadWrite> depart = memorymanager.getNewGameState();
		GameState.copy(state, last);
		dstarlite.computeNewPath(depart.robot.getPosition(), arrivee);
		do {
			GameState.copy(last.getReadOnly(), depart);
		}
		while(!process(depart));
	}
	
	public void updatePath() throws PathfindingException
	{
		GameState<RobotChrono,ReadWrite> depart = memorymanager.getNewGameState();
		GameState.copy(state, last);
		dstarlite.updatePath(depart.robot.getPosition());
		do {
			GameState.copy(last.getReadOnly(), depart);
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
	private synchronized boolean process(GameState<RobotChrono,ReadWrite> depart) throws PathfindingException
	{
		int hash_depart = dstarlite.getHashDebut();
		int hash_arrivee = dstarlite.getHashArrivee();
		// optimisation si depart == arrivee
		
		if(hash_depart == hash_arrivee)
		{
			// Le memory manager impose de détruire les gamestates non utilisés.
			memorymanager.destroyGameState(depart);
			return true;
		}

		closedset.clear();
		// plus rapide que des arraycopy
		// TODO refaire
		for(int i = 0; i < nb_max_element; i++)
		{
			came_from_arc[i] = null;
			came_from[i] = -1;
			g_score[i] = Integer.MAX_VALUE;
			f_score[i] = Integer.MAX_VALUE;
		}
		
		openset.clear();
		openset.add(depart);	// The set of tentative nodes to be evaluated, initially containing the start node
		g_score[hash_depart] = 0;	// Cost from start along best known path.
		// Estimated total cost from start to goal through y.
		f_score[hash_depart] = g_score[hash_depart] + dstarlite.heuristicCostThetaStar(hash_depart);
		
		GameState<RobotChrono,ReadWrite> current = null;

		// iterator?
		
		while (!openset.isEmpty())
		{
			int min_score = Integer.MAX_VALUE;
			int potential_min, index_min = 0;
			int hash_current = -1;
			GameState<RobotChrono,ReadWrite> tmp;
			
			// On recherche l'élément d'openset qui minimise f_score
			int max_value = openset.size();
			for(int i = 0; i < max_value; i++)
			{
				tmp = openset.get(i);
				int tmp_hash = tmp.robot.getPositionGridSpace();
				potential_min = f_score[tmp_hash];
				if(min_score >= potential_min)
				{
					min_score = potential_min;
					current = tmp;
					index_min = i;
					hash_current = tmp_hash;
				}
			}
			openset.remove(index_min);
			
			// élément déjà fait
			// cela parce qu'il y a des doublons dans openset
			if(closedset.get(hash_current))
			{
//				if(arcmanager instanceof StrategyArcManager)
//					log.debug("Destruction de: "+current.getIndiceMemoryManager(), this);
				memorymanager.destroyGameState(current);
				continue;
			}
			
			// Si on est arrivé, on reconstruit le chemin
			if(hash_current == hash_arrivee)
			{
//				if(arcmanager instanceof StrategyArcManager)
//					log.debug("Destruction de: "+current.getIndiceMemoryManager(), this);
				memorymanager.empty();
				if(reconstruct(current.getReadOnly()))
					return false;
				return true;
			}
			else if(reconstruct(current.getReadOnly()))
			{
				memorymanager.empty();
				return false;
			}


			closedset.set(hash_current);
		    
			
			// On parcourt les voisins de current.
			arcmanager.reinitIterator(current.robot.getPositionGridSpace());

			boolean encoreUnSuccesseur = false;
			encoreUnSuccesseur = arcmanager.hasNext();
			
			/** Puisque current ne pourra pas être réutilisé,
			 * il faut le détruire
			 */
			if(!encoreUnSuccesseur)
				memorymanager.destroyGameState(current);
			
			boolean reuseOldSuccesseur = false;
			GameState<RobotChrono,ReadWrite> successeur = null;
			
			while(encoreUnSuccesseur)
			{
				LocomotionArc voisin = arcmanager.next();
				encoreUnSuccesseur = arcmanager.hasNext();
				
				if(!encoreUnSuccesseur) // le dernier successeur
				{
					if(reuseOldSuccesseur)
						memorymanager.destroyGameState(successeur);
					successeur = current;
				}
				else
				{
					if(!reuseOldSuccesseur)
						successeur = memorymanager.getNewGameState();
					GameState.copy(current.getReadOnly(), successeur);
				}
				reuseOldSuccesseur = false;
				
				// successeur est modifié lors du "distanceTo"
				// si ce successeur dépasse la date limite, on l'annule
				int tentative_g_score;
//				try {
					tentative_g_score = g_score[hash_current] + arcmanager.distanceTo(successeur, voisin);
					if(tentative_g_score - g_score[hash_current] < 0)
						log.critical("Distance négative! "+(tentative_g_score - g_score[hash_current]));
//					log.debug("Tentative pour "+voisin+": "+tentative_g_score, this);
/*				} catch (ScriptException | FinMatchException e) {
					if(encoreUnSuccesseur)
						reuseOldSuccesseur = true;
					else
						memorymanager.destroyGameState(successeur);
					continue;
				}*/

				int hash_successeur = successeur.robot.getPositionGridSpace();

//				if(arcmanager instanceof StrategyArcManager)
//					log.debug(voisin+" donne "+hash_successeur, this);
				
				if(closedset.get(hash_successeur)) // si closedset contient ce hash
				{
//					if(arcmanager instanceof StrategyArcManager)
//						log.debug("Destruction de: "+successeur.getIndiceMemoryManager(), this);
					if(encoreUnSuccesseur)
						reuseOldSuccesseur = true;
					else
						memorymanager.destroyGameState(successeur);
					continue;
				}
				
				if(tentative_g_score < g_score[hash_successeur])
				{
					came_from[hash_successeur] = hash_current;
					came_from_arc[hash_successeur] = voisin;
					g_score[hash_successeur] = tentative_g_score;
					f_score[hash_successeur] = tentative_g_score + dstarlite.heuristicCostThetaStar(successeur.robot.getPositionGridSpace());
					openset.add(successeur);
					// reuseOldSuccesseur reste à false
				}
				else
				{
					if(encoreUnSuccesseur)
						reuseOldSuccesseur = true;
					else
						memorymanager.destroyGameState(successeur);
				}
			}
			
//			if(arcmanager instanceof StrategyArcManager)
//				log.debug("Destruction de: "+current.getIndiceMemoryManager(), this);
//			memorymanager.destroyGameState(current);
		}
		
		// Pathfinding terminé sans avoir atteint l'arrivée
		memorymanager.empty();
		return true;
		
	}
	
	private boolean reconstruct(GameState<RobotChrono, ReadOnly> best)
	{
		if(chemin.needToStartAgain())
			return true;
		int hash = best.robot.getPositionGridSpace();
		cheminTmp.clear();
		int noeud_parent = came_from[hash];
		LocomotionArc arc_parent = came_from_arc[hash];
		while (noeud_parent != -1)
		{
			cheminTmp.add(0, arc_parent);
			arc_parent = came_from_arc[noeud_parent];
			noeud_parent = came_from[noeud_parent];
		}			
		synchronized(chemin)
		{
			chemin.set(cheminTmp);
		}
		GameState.copy(best, last);
		return false;
	}

	@Override
	public void updateConfig(Config config)
	{
	}

	@Override
	public void useConfig(Config config)
	{
	}
	
}