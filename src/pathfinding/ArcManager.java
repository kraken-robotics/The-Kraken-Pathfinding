package pathfinding;

import exceptions.FinMatchException;
import exceptions.strategie.ScriptException;
import robot.RobotChrono;
import strategie.GameState;

/**
 * Réalise les calculs entre différents Arc
 * @author pf
 *
 */

public interface ArcManager {

	public void reinitIterator(GameState<RobotChrono> gamestate);

	public boolean hasNext(GameState<RobotChrono> state);
	
	/**
	 * Donne l'arc pour aller au noeud suivant
	 * @return
	 */
	public <A> A next();
	
	/** 
	 * Donne la distance exacte entre les deux points.
	 * Exécute un script pour l'arbre des possibles.
	 * @param other
	 * @return
	 * @throws ScriptException 
	 */
	public int distanceTo(GameState<RobotChrono> state, Arc arc) throws FinMatchException, ScriptException;
	
	/**
	 * Evalue la distance entre deux sommets.
	 * Plus exactement, on doit pouvoir minorer la distance réelle.
	 * @param other
	 * @return
	 */
	public int heuristicCost(GameState<RobotChrono> state);

	public int getHash(GameState<RobotChrono> state);	
	
	public boolean isArrive(int hash);
	
	public int getNoteReconstruct(int hash);
	
}
