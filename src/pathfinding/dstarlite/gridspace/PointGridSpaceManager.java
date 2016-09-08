package pathfinding.dstarlite.gridspace;

import container.Service;
import utils.Config;
import utils.Log;
import utils.Vec2;
import utils.permissions.ReadOnly;

/**
 * S'occupe de gérer les PointGridSpace
 * @author pf
 *
 */

public class PointGridSpaceManager implements Service
{
	private static final int X_MAX = PointGridSpace.NB_POINTS_POUR_TROIS_METRES - 1;
	private static final int Y_MAX = PointGridSpace.NB_POINTS_POUR_DEUX_METRES - 1;
	
	protected Log log;
	private static PointGridSpace[] allPoints = new PointGridSpace[PointGridSpace.NB_POINTS];
	
	public PointGridSpaceManager(Log log)
	{
		this.log = log;
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
		if(x < 0 || x > X_MAX || y < 0 || y > Y_MAX)
			return null;

		return get(index(x,y));
	}
	
	/**
	 * Renvoie l'indice du gridpoint le plus proche de cette position
	 * @param p
	 * @return
	 */
	public PointGridSpace get(Vec2<ReadOnly> p)
	{
		int y = (int) Math.round(p.y / PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS);
		int x = (int) Math.round((p.x + 1500) / PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS);
		
		if(x < 0 || x > X_MAX || y < 0 || y > Y_MAX)
			return null;

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

		if(x < 0 || x > X_MAX || y < 0 || y > Y_MAX)
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
