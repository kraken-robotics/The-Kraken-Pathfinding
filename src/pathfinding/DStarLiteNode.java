package pathfinding;

/**
 * Un nœud du pathfinding
 * @author pf
 *
 */

public class DStarLiteNode {

	public final int gridpoint;
	public Cle cle = new Cle();
	public int g = Integer.MAX_VALUE, rhs = Integer.MAX_VALUE;
	public long nbPF = 1;
	
	public DStarLiteNode(int gridpoint)
	{
		this.gridpoint = gridpoint;
	}
	
	@Override
	public int hashCode()
	{
		return gridpoint;
	}
	
	@Override
	public boolean equals(Object o)
	{
		return hashCode() == o.hashCode();
	}
	
}