package table;

/**
 * Énumération des directions dans un gridspace à 8 voisins.
 * @author pf
 *
 */

public enum DirectionGridSpace {
	
	NO(1414),
	N(1000),
	NE(1414),
	O(1000),
	E(1000),
	SO(1414),
	S(1000),
	SE(1414);
	
	private final int distance;
	
	private DirectionGridSpace(int distance)
	{
		this.distance = distance;
	}

	public int getDistance()
	{
		return distance;
	}
	
}
