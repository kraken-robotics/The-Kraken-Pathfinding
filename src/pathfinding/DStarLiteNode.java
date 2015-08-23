package pathfinding;

/**
 * Un nœud du pathfinding
 * @author pf
 *
 */

public class DStarLiteNode {

	public int hash;
	public int gridpoint;
	public Cle cle = new Cle();
	public int g = Integer.MAX_VALUE, rhs = Integer.MAX_VALUE;
	private DStarLiteNode[] voisins = null;
	
	private static int nextHash = 0;
	
	public DStarLiteNode(int gridpoint)
	{
		hash = nextHash++;
		this.gridpoint = gridpoint;
	}

	public static void reinitHash()
	{
		nextHash = 0;
	}
	
	public DStarLiteNode getVoisin(int direction, GridSpace gridspace)
	{
		if(voisins == null)
		{
			voisins = new DStarLiteNode[8];
			for(int i = 0; i < 8; i++)
			{
				voisins[i] = new DStarLiteNode(gridspace.getGridPointVoisin(gridpoint, direction));
			}
		}
		return voisins[direction];
	}
	
}