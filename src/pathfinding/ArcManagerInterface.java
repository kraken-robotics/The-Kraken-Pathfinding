package pathfinding;

import robot.RobotChrono;
import strategie.GameState;

/**
 * Interface du NodeManager.
 * C'est lui qui s'occupe de calculer les voisins d'un sommet.
 * @author pf
 *
 */

public interface ArcManagerInterface {

	public void reinitIterator(GameState<RobotChrono> gamestate);

	public boolean hasNext();
	
	/**
	 * Donne l'arc pour aller au noeud suivant
	 * @return
	 */
	public ArcInterface next();
	
	/**
	 * Renvoie vrai s'il y a égalité.
	 * @param state1
	 * @param state2
	 * @return
	 */
	public boolean areEquals(GameState<RobotChrono> state1, GameState<RobotChrono> state2);
	
	/** 
	 * Donne la distance exacte entre les deux points.
	 * Exécute un script pour l'arbre des possibles.
	 * @param other
	 * @return
	 */
	public double distanceTo(GameState<RobotChrono> state, ArcInterface arc);
	
	/**
	 * Evalue la distance entre deux sommets.
	 * Plus exactement, on doit pouvoir minorer la distance réelle.
	 * @param other
	 * @return
	 */
	public double heuristicCost(GameState<RobotChrono> state1, GameState<RobotChrono> state2);

}
