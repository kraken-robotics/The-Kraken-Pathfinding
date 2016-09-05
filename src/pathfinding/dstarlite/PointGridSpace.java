package pathfinding.dstarlite;

import utils.Vec2;
import utils.permissions.ReadOnly;

/**
 * Un point du gridspace
 * @author pf
 *
 */

public class PointGridSpace
{
	public static final int PRECISION = 6;
	public static final int NB_POINTS_POUR_TROIS_METRES = (1 << PRECISION);
	public static final int NB_POINTS_POUR_DEUX_METRES = (int) ((1 << PRECISION)*2./3.)+1;
	public static final double DISTANCE_ENTRE_DEUX_POINTS = 3000./(NB_POINTS_POUR_TROIS_METRES-1);
	public static final int DISTANCE_ENTRE_DEUX_POINTS_1024 = (int)(1024*3000./(NB_POINTS_POUR_TROIS_METRES-1));
	public static final int NB_POINTS = NB_POINTS_POUR_DEUX_METRES * NB_POINTS_POUR_TROIS_METRES;
	private static final int X_MAX = NB_POINTS_POUR_TROIS_METRES - 1;
	private static final int Y_MAX = NB_POINTS_POUR_DEUX_METRES - 1;

	public int x, y;

	public PointGridSpace(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	public PointGridSpace(Vec2<ReadOnly> p)
	{
		x = (int) Math.round(p.y / DISTANCE_ENTRE_DEUX_POINTS);
		y = (int) Math.round((p.x+1500) / DISTANCE_ENTRE_DEUX_POINTS);
	}
	
	@Override
	public int hashCode()
	{
		return (x << PRECISION) + y;
	}
	
	/**
	 * On utilise la distance octile pour l'heuristique (surtout parce que c'est rapide)
	 * @param pointA
	 * @param pointB
	 * @return
	 */
	public static final int distanceHeuristiqueDStarLite(PointGridSpace pointA, PointGridSpace pointB)
	{
		int dx = Math.abs(pointA.x - pointB.x);
		int dy = Math.abs(pointA.y - pointB.y);
		return 1000 * Math.max(dx, dy) + 414 * Math.min(dx, dy);
	}
	
	/**
	 * Récupère le voisin de "point" dans la direction indiquée.
	 * Renvoie -1 si un tel voisin est hors table
	 * @param point
	 * @param direction
	 * @return
	 */
	public PointGridSpace getGridPointVoisin(Direction direction)
	{
		int x = this.x + direction.deltaX;
		int y = this.y + direction.deltaY;

		if(x < 0 || x > X_MAX || y < 0 || y > Y_MAX)
			return null;
		return new PointGridSpace(x,y);		
	}

	public static final PointGridSpace getGridPointX(Vec2<ReadOnly> p)
	{
		return new PointGridSpace(Math.round((p.x+1500) / PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS);
	}

	public static final int getGridPointY(Vec2<ReadOnly> p)
	{
		return (int) Math.round(p.y / PointGridSpace.DISTANCE_ENTRE_DEUX_POINTS);
	}

	public static final int getGridPoint(int x, int y)
	{
		return (y << PRECISION) + x;
	}

}
