package pathfinding;

import exceptions.FinMatchException;
import exceptions.UnknownScriptException;
import exceptions.Locomotion.UnableToMoveException;
import exceptions.serial.SerialConnexionException;
import robot.RobotChrono;
import strategie.GameState;

/**
 * Interface du NodeManager.
 * C'est lui qui s'occupe de calculer les voisins d'un sommet.
 * @author pf
 *
 */

public abstract class ArcManager {

	public abstract void reinitIterator(GameState<RobotChrono> gamestate);

	public abstract boolean hasNext(GameState<RobotChrono> state);
	
	/**
	 * Donne l'arc pour aller au noeud suivant
	 * @return
	 */
	public abstract Arc next();
	
	/** 
	 * Donne la distance exacte entre les deux points.
	 * Exécute un script pour l'arbre des possibles.
	 * @param other
	 * @return
	 * @throws UnableToMoveException 
	 */
	public abstract int distanceTo(GameState<RobotChrono> state, Arc arc) throws FinMatchException, UnknownScriptException, SerialConnexionException, UnableToMoveException;
	
	/**
	 * Evalue la distance entre deux sommets.
	 * Plus exactement, on doit pouvoir minorer la distance réelle.
	 * @param other
	 * @return
	 */
	public abstract int heuristicCost(GameState<RobotChrono> state);

	public abstract int getHash(GameState<RobotChrono> state);	
	
	public abstract boolean isArrive(int hash);
	
	public abstract int getNoteReconstruct(int hash);
	
}
