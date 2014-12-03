package enums;

/**
 * Caractérise une liaison entre deux noeuds de la grille du pathfinding
 * @author pf
 *
 */

public enum NodesConnection {
	ALWAYS_IMPOSSIBLE(false), 	// Impossible à cause d'obstacles fixes
	TMP_IMPOSSIBLE(false),		// Impossible à cause d'obstacles mobiles
	POSSIBLE(true),			// Possible
	UNKNOW(false);				// Status inconnu (TMP_IMPOSSIBLE ou POSSIBLE)
	
	private boolean traversable;
	
	// Plus user-friendly pour la recherche de chemin
	private NodesConnection(boolean traversable)
	{
		this.traversable = traversable;
	}
	
	public boolean isTraversable()
	{
		return traversable;
	}
	
}
