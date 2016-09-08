package pathfinding.dstarlite.gridspace;

public enum Direction {

	NO(-1,1),SE(1,-1),NE(1,1),SO(-1,-1),
	N(0,1),S(0,-1),O(-1,0),E(1,0);

	public final int deltaX, deltaY;
	
	private Direction(int deltaX, int deltaY)
	{
		this.deltaX = deltaX;
		this.deltaY = deltaY;
	}
	
	/**
	 * Cette direction est-elle diagonale ?
	 * @return
	 */
	public boolean isDiagonal()
	{
		return ordinal() < 4;
	}
	
	/**
	 * Récupère la direction associée à un déplacement exprimé en deltaX et deltaY
	 * @param deltaX
	 * @param deltaY
	 * @return
	 */
	static Direction convertToDirection(int deltaX, int deltaY)
	{
		for(Direction d : values())
			if(deltaX == d.deltaX && deltaY == d.deltaY)
				return d;
		return null; // pas reconnu
	}

	/**
	 * Fournit la direction opposée
	 * @return
	 */
	public Direction getOppose()
	{
		return values()[ordinal() ^ 1]; // ouais ouais
	}
}
