package pathfinding;

/**
 * Un nœud du pathfinding
 * @author pf
 *
 */

public class DStarLiteNode {

	public int gridpoint;
	public Cle cle = new Cle();
	public int g = Integer.MAX_VALUE, rhs = Integer.MAX_VALUE;
	private DStarLiteNode[] voisins = null;
	
	public DStarLiteNode(int gridpoint)
	{
		this.gridpoint = gridpoint;
	}

	public DStarLiteNode getVoisin(int direction, GridSpace gridspace)
	{
		if(voisins == null)
		{
			voisins = new DStarLiteNode[8];
			for(int i = 0; i < 8; i++)
			{
				int voisin = gridspace.getGridPointVoisin(gridpoint, direction);
				if(voisin >= 0)
					voisins[i] = new DStarLiteNode(voisin);
			}
		}
		return voisins[direction];
	}
	
}