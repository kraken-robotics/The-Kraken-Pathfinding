package enums;

/**
 * Caractérise une liaison entre deux noeuds de la grille du pathfinding
 * @author pf
 *
 */

public enum NodesConnection {
	ALWAYS_IMPOSSIBLE, 	// Impossible à cause d'obstacles fixes
	TMP_IMPOSSIBLE,		// Impossible à cause d'obstacles mobiles
	POSSIBLE,			// Possible
	UNKNOW;				// Status inconnu (TMP_IMPOSSIBLE ou POSSIBLE)
	
}
