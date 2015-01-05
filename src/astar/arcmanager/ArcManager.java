package astar.arcmanager;

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

public interface ArcManager {

	/**
	 * Permet d'obtenir les voisins
	 * @param gamestate
	 * @throws MemoryManagerException
	 */
	public void reinitIterator(GameState<RobotChrono> gamestate) throws MemoryManagerException;

	/**
	 * Renvoie true s'il y a un autre voisin
	 * @param state
	 * @return
	 */
	public boolean hasNext(GameState<RobotChrono> state);
	
	/**
	 * Donne le prochain voisin
	 * @return
	 */
	public <A> A next();
	
	/** 
	 * Donne la distance exacte entre les deux points.
	 * Exécute un script pour l'arbre des possibles.
	 * Il y a MODIFICATION de state
	 * @param other
	 * @return
	 * @throws ScriptException 
	 */
	public int distanceTo(GameState<RobotChrono> state, Arc arc) throws FinMatchException, ScriptException;
	
	/**
	 * Evalue la distance entre deux sommets.
	 * Plus exactement, on doit pouvoir minorer la distance réelle.
	 * State n'est pas modifié.
	 * @param other
	 * @return
	 */
	public int heuristicCost(GameState<RobotChrono> state);

	/**
	 * Enregistre un nouveau hash.
	 * @param state
	 * @return
	 */
	public int getHashAndCreateIfNecessary(GameState<RobotChrono> state);
	
	/**
	 * Récupère un hash existant.
	 * @param state
	 * @return
	 * @throws ArcManagerException
	 */
	public int getHash(GameState<RobotChrono> state) throws ArcManagerException;
	
	/**
	 * Sommes-nous arrivés?
	 * @param hash
	 * @return
	 */
	public boolean isArrive(int hash);
	
	/**
	 * Si on n'a pas atteint le point d'arrivée, il faut noter les hash
	 * de manière à avoir un point proche de l'arrivée.
	 * @param hash
	 * @return
	 */
	public int getNoteReconstruct(int hash);
	
}
