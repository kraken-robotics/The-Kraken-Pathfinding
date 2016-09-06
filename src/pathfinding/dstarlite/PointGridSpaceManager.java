package pathfinding.dstarlite;

import container.Service;
import utils.Config;
import utils.Vec2;
import utils.permissions.ReadOnly;

/**
 * S'occupe de gérer les PointGridSpace
 * @author pf
 *
 */

public class PointGridSpaceManager implements Service
{
	private static PointGridSpace[] allPoints = new PointGridSpace[PointGridSpace.NB_POINTS];
	
	public PointGridSpaceManager()
	{
		for(int i = 0; i < PointGridSpace.NB_POINTS; i++)
			allPoints[i] = new PointGridSpace(i);			
	}
	
	/**
	 * Récupère un PointGridSpace à partir de ses coordonnées
	 * @param x
	 * @param y
	 * @return
	 */
	public PointGridSpace get(int x, int y)
	{
		return allPoints[index(x,y)];
	}
	
	/**
	 * Renvoie l'indice du gridpoint le plus proche de cette position
	 * @param p
	 * @return
	 */
	public PointGridSpace get (Vec2<ReadOnly> p)
	{
		int y = (int) Math.round(p.y / PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS);
		int x = (int) Math.round((p.x + 1500) / PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS);
		return allPoints[index(x,y)];
	}
	
	private int index(int x, int y)
	{
		return (y << PointGridSpace.PRECISION) + x;
	}

	/**
	 * Récupère un gridpointspace à partir de son hashcode
	 * @param i
	 * @return
	 */
	public PointGridSpace get(int i)
	{
		return allPoints[i];
	}
	
	/**
	 * Récupère le voisin de "point" dans la direction indiquée.
	 * Renvoie -1 si un tel voisin est hors table
	 * @param point
	 * @param direction
	 * @return
	 */
	public PointGridSpace getGridPointVoisin(PointGridSpace point, Direction direction)
	{
		int x = point.x + direction.deltaX;
		int y = point.y + direction.deltaY;

		if(x < 0 || x > PointGridSpace.X_MAX || y < 0 || y > PointGridSpace.Y_MAX)
			return null;
		return get(x,y);
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
}
