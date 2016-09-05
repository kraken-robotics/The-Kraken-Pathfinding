package pathfinding.dstarlite;

/**
 * Un nœud du D* Lite.
 * @author pf
 *
 */

public class DStarLiteNode {

	public final PointGridSpace gridpoint;
	public Cle cle = new Cle();
	public int g = Integer.MAX_VALUE, rhs = Integer.MAX_VALUE;
	
	/**
	 * "done" correspond à l'appartenance à U dans l'algo du DStarLite
	 */
	public boolean done = false;
	public long nbPF = 0;
	
	public DStarLiteNode(PointGridSpace gridpoint)
	{
		this.gridpoint = gridpoint;
	}
	
	@Override
	public final int hashCode()
	{
		return gridpoint.hashCode();
	}
	
	@Override
	public final boolean equals(Object o)
	{
		return gridpoint.hashCode() == o.hashCode();
	}
	
	@Override
	public String toString()
	{
		int x = gridpoint.x;
		int y = gridpoint.y;
		return x+" "+y+" ("+cle+")";
	}
	
}