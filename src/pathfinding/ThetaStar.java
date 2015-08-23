package pathfinding;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.BitSet;

import buffer.DataForSerialOutput;
import permissions.ReadWrite;
import planification.astar.arc.PathfindingNodes;
import container.Service;
import exceptions.FinMatchException;
import exceptions.MemoryManagerException;
import exceptions.PathfindingException;
import exceptions.PathfindingRobotInObstacleException;
import robot.RobotChrono;
import strategie.GameState;
import utils.Config;
import utils.Log;

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
	
	protected Log log;
	private DStarLite dstarlite;
	private DataForSerialOutput serie;
	private MemoryManager memorymanager;
	private ArcManager arcmanager;
	
	/**
	 * Constructeur du ThetaStar
	 */
	public ThetaStar(Log log, DStarLite dstarlite, ArcManager arcmanager, DataForSerialOutput serie, MemoryManager memorymanager)
	{
		this.log = log;
		this.dstarlite = dstarlite;
		this.serie = serie;
		this.arcmanager = arcmanager;
		this.memorymanager = memorymanager;
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
	public synchronized ArrayList<LocomotionArc> computePath(GameState<RobotChrono,ReadWrite> depart, PathfindingNodes endNode, boolean shoot_game_element) throws PathfindingException, PathfindingRobotInObstacleException, FinMatchException, MemoryManagerException
	{
//			log.debug("Recherche de chemin entre "+pointDepart+" et "+indice_point_arrivee, this);
		ArrayList<LocomotionArc> cheminArc = process(depart);

//			log.debug("Recherche de chemin terminée", this);
		return cheminArc;
	}
	
	/**
	 * Le calcul du ThetaStar
	 * @param depart
	 * @param arcmanager
	 * @param shouldReconstruct
	 * @return
	 * @throws PathfindingException
	 * @throws MemoryManagerException
	 */
	private ArrayList<LocomotionArc> process(GameState<RobotChrono,ReadWrite> depart) throws PathfindingException, MemoryManagerException
	{
		int hash_depart = dstarlite.getHashDebut();
		int hash_arrivee = dstarlite.getHashArrivee();
		// optimisation si depart == arrivee
		
		if(hash_depart == hash_arrivee)
		{
			// Le memory manager impose de détruire les gamestates non utilisés.
			memorymanager.destroyGameState(depart);
			return new ArrayList<LocomotionArc>();
		}

		closedset.clear();
		// plus rapide que des arraycopy
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
				return reconstruct(hash_current);
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
				
				try {
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
				} catch (FinMatchException e1) {
					// ne devrait pas arriver!
					throw new PathfindingException();
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
		
//		log.debug("Reconstruction!", this);
		
		/**
		 * Même si on n'a pas atteint l'objectif, on reconstruit un chemin partiel
		 */
		int note_best = Integer.MIN_VALUE;
		int note_f_best = Integer.MAX_VALUE;
		int best = -1;
		// On maximise le nombre de points qu'on fait.
		// En cas d'égalité, on prend le chemin le plus rapide.
		
		for(int h = 0; h < nb_max_element; h++)
			if(closedset.get(h)) // si ce noeud a été parcouru (sinon getNoteReconstruct va paniquer)
			{
				int potentiel_note_best = arcmanager.getNoteReconstruct(h);
				int potentiel_note_f_best = f_score[h];
//				log.debug(potentiel_note_best+" en "+potentiel_note_f_best, this);
				if(potentiel_note_best > note_best || potentiel_note_best == note_best && potentiel_note_f_best < note_f_best)
				{
					best = h;
					note_best = potentiel_note_best;
					note_f_best = potentiel_note_f_best;
				}
			}
		
		// best est nécessairement non nul car closedset contient au moins le point de départ
		return reconstruct(best);
		
	}

	private final ArrayList<LocomotionArc> reconstruct(int hash)
	{
		ArrayList<LocomotionArc> chemin = new ArrayList<LocomotionArc>();
		int noeud_parent = came_from[hash];
		LocomotionArc arc_parent = came_from_arc[hash];
		while (noeud_parent != -1)
		{
			chemin.add(0, arc_parent);
			arc_parent = came_from_arc[noeud_parent];
			noeud_parent = came_from[noeud_parent];
		}
		return chemin;	//  reconstructed path
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