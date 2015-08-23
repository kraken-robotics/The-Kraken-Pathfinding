package pathfinding;

/**
 * Énumération des directions dans un gridspace à 8 voisins.
 * @author pf
 *
 */

public enum DirectionGridSpace {
	
	NO,
	SE,
	NE,
	SO,
	N,
	S,
	O,
	E;
	
	public static int distance(int i) {
		if(i < 4)
			return 1414;
		else
			return 1000;
	}
	
}
