package astar.arcmanager;

import astar.AStarId;
import astar.MemoryManager;
import astar.arc.Arc;
import exceptions.ArcManagerException;
import exceptions.FinMatchException;
import exceptions.MemoryManagerException;
import exceptions.ScriptException;
import robot.RobotChrono;
import strategie.GameState;

/**
 * Réalise les calculs entre différents Arc
 * @author pf
 *
 */

public abstract class ArcManager {

	private int id;
	private MemoryManager memorymanager;

	public ArcManager(AStarId id, MemoryManager memorymanager)
	{
		this.id = id.ordinal();
		this.memorymanager = memorymanager;
	}
	
	/**
	 * Permet d'obtenir les voisins
	 * @param gamestate
	 * @throws MemoryManagerException
	 */
	public abstract void reinitIterator(GameState<RobotChrono> gamestate) throws MemoryManagerException;

	/**
	 * Renvoie true s'il y a un autre voisin
	 * @param state
	 * @return
	 */
	public abstract boolean hasNext();
	
	/**
	 * Donne le prochain voisin
	 * @return
	 */
	public abstract <A> A next();
	
	/** 
	 * Donne la distance exacte entre les deux points.
	 * Exécute un script pour l'arbre des possibles.
	 * Il y a MODIFICATION de state
	 * @param other
	 * @return
	 * @throws ScriptException 
	 */
	public abstract int distanceTo(GameState<RobotChrono> state, Arc arc) throws FinMatchException, ScriptException;
	
	/**
	 * Evalue la distance entre deux sommets.
	 * Plus exactement, on doit pouvoir minorer la distance réelle.
	 * State n'est pas modifié.
	 * @param other
	 * @return
	 */
	public abstract int heuristicCost(GameState<RobotChrono> state);

	/**
	 * Enregistre un nouveau hash.
	 * @param state
	 * @return
	 */
	public abstract int getHashAndCreateIfNecessary(GameState<RobotChrono> state);
	
	/**
	 * Récupère un hash existant.
	 * @param state
	 * @return
	 * @throws ArcManagerException
	 */
	public abstract int getHash(GameState<RobotChrono> state) throws ArcManagerException;
	
	/**
	 * Sommes-nous arrivés?
	 * @param hash
	 * @return
	 */
	public abstract boolean isArrive(int hash);
	
	/**
	 * Si on n'a pas atteint le point d'arrivée, il faut noter les hash
	 * de manière à avoir un point proche de l'arrivée.
	 * @param hash
	 * @return
	 */
	public abstract int getNoteReconstruct(int hash);

	public void destroyGameState(GameState<RobotChrono> state) throws MemoryManagerException
	{
		memorymanager.destroyGameState(state, id);
	}

	public GameState<RobotChrono> getNewGameState() throws FinMatchException
	{
		return memorymanager.getNewGameState(id);
	}
	
	public void empty()
	{
		memorymanager.empty(id);
	}

}
