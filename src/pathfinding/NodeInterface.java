package pathfinding;

/**
 * Interface d'un sommet du pathfinding.
 * Un gamestate dans l'arbre des possibles, un Vec2 dans le pathfinding.
 * @author pf
 *
 */

public interface NodeInterface {

	/** 
	 * Donne la distance exacte entre les deux points.
	 * Exécute un script pour l'arbre des possibles.
	 * @param other
	 * @return
	 */
	public double distanceTo(NodeInterface other);
	
	/**
	 * Evalue la distance entre deux sommets.
	 * Plus exactement, on doit pouvoir minorer la distance réelle.
	 * @param other
	 * @return
	 */
	public double heuristicCost(NodeInterface other);
	
}
