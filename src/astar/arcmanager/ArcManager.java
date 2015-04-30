package astar.arcmanager;

import permissions.ReadOnly;
import permissions.ReadWrite;
import container.Service;
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

public abstract class ArcManager<A extends Arc> implements Service {

	private int id;
	private MemoryManager memorymanager;

	public ArcManager(AStarId id, MemoryManager memorymanager)
	{
		this.id = id.ordinal();
		this.memorymanager = memorymanager;
	}
	
	@Override
	public void updateConfig()
	{
		memorymanager.updateConfig();
	}
	
	/**
	 * Permet d'obtenir les voisins
	 * @param gamestate
	 * @throws MemoryManagerException
	 */
	public abstract void reinitIterator(GameState<RobotChrono,ReadOnly> gamestate) throws MemoryManagerException;

	/**
	 * Renvoie true s'il y a un autre voisin
	 * @param state
	 * @return
	 * @throws FinMatchException 
	 */
	public abstract boolean hasNext() throws FinMatchException;
	
	/**
	 * Donne le prochain voisin
	 * @return
	 */
	public abstract A next();
	
	/** 
	 * Donne la distance exacte entre les deux points.
	 * Exécute un script pour l'arbre des possibles.
	 * Il y a MODIFICATION de state
	 * @param other
	 * @return
	 * @throws ScriptException 
	 */
	public abstract int distanceTo(GameState<RobotChrono,ReadWrite> state, A arc) throws FinMatchException, ScriptException;
	
	/**
	 * Evalue la distance entre deux sommets.
	 * Plus exactement, on doit pouvoir minorer la distance réelle.
	 * State n'est pas modifié.
	 * @param other
	 * @return
	 */
	public abstract int heuristicCost(GameState<RobotChrono,ReadOnly> state);

	/**
	 * Enregistre un nouveau hash.
	 * @param state
	 * @return
	 */
	public abstract int getHashAndCreateIfNecessary(GameState<RobotChrono,ReadOnly> state);
	
	/**
	 * Récupère un hash existant.
	 * @param state
	 * @return
	 * @throws ArcManagerException
	 */
	public abstract int getHash(GameState<RobotChrono,ReadOnly> state) throws ArcManagerException;
	
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

	/**
	 * Surcouche du memory manager. Signale qu'un état est de nouveau disponible.
	 * @param state
	 * @throws MemoryManagerException
	 */
	public void destroyGameState(GameState<RobotChrono,ReadWrite> state) throws MemoryManagerException
	{
		memorymanager.destroyGameState(state, id);
	}

	/**
	 * Surcouche du memory manager. Réutilise un état.
	 * @return
	 * @throws FinMatchException
	 */
	public GameState<RobotChrono,ReadWrite> getNewGameState() throws FinMatchException
	{
		return memorymanager.getNewGameState(id);
	}
	
	/**
	 * Vide le memory manager. Beaucoup plus rapide que plein d'appels à destroyGameState!
	 */
	public void empty()
	{
		memorymanager.empty(id);
	}

}
