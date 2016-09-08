package pathfinding.dstarlite.gridspace;

/**
 * Une structure utilisée par le GridSpace
 * @author pf
 *
 */

public class PointDirige
{
	public final PointGridSpace point;
	public final Direction dir;
	
	PointDirige(PointGridSpace point, Direction dir)
	{
		this.point = point;
		this.dir = dir;
	}
	
	@Override
	public int hashCode()
	{
		return (point.hashCode() << 3) + dir.ordinal();
	}
	
	@Override
	public boolean equals(Object d)
	{
		return d instanceof PointDirige && hashCode() == ((PointDirige)d).hashCode();
	}

}
