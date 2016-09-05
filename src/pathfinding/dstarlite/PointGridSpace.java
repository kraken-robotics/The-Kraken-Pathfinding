package pathfinding.dstarlite;

import utils.Vec2;
import utils.permissions.ReadOnly;
import utils.permissions.ReadWrite;

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
	public static final int X_MAX = NB_POINTS_POUR_TROIS_METRES - 1;
	public static final int Y_MAX = NB_POINTS_POUR_DEUX_METRES - 1;

	public int x, y;

	public PointGridSpace(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Renvoie l'indice du gridpoint le plus proche de cette position
	 * @param p
	 * @return
	 */
	public PointGridSpace(Vec2<ReadOnly> p)
	{
		y = (int) Math.round(p.y / DISTANCE_ENTRE_DEUX_POINTS);
		x = (int) Math.round((p.x+1500) / DISTANCE_ENTRE_DEUX_POINTS);
	}
	
	/**
	 * Construit à partir du hashCode
	 * @param i
	 */
	public PointGridSpace(int i)
	{
		y = i >> PRECISION;
		x = i & (NB_POINTS_POUR_TROIS_METRES - 1);
	}

	@Override
	public int hashCode()
	{
		return (y << PRECISION) + x;
	}
	
	/**
	 * On utilise la distance octile pour l'heuristique (surtout parce que c'est rapide)
	 * @param pointA
	 * @param pointB
	 * @return
	 */
	public final int distanceHeuristiqueDStarLite(PointGridSpace point)
	{
		int dx = Math.abs(x - point.x);
		int dy = Math.abs(y - point.y);
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

	public static final int getGridPointX(Vec2<ReadOnly> p)
	{
		return (int) Math.round((p.x + 1500) / DISTANCE_ENTRE_DEUX_POINTS);
	}

	public static final int getGridPointY(Vec2<ReadOnly> p)
	{
		return (int) Math.round(p.y / DISTANCE_ENTRE_DEUX_POINTS);
	}

	public static final int getGridPoint(int x, int y)
	{
		return (y << PRECISION) + x;
	}

	public static final Vec2<ReadOnly> computeVec2(PointGridSpace gridpoint)
	{
		Vec2<ReadWrite> out = new Vec2<ReadWrite>();
		computeVec2(out, gridpoint);
		return out.getReadOnly();
	}

	public static final void computeVec2(Vec2<ReadWrite> v, PointGridSpace gridpoint)
	{
		v.x = ((gridpoint.x * DISTANCE_ENTRE_DEUX_POINTS_1024) >> 10) - 1500;
		v.y = (gridpoint.y * DISTANCE_ENTRE_DEUX_POINTS_1024) >> 10;
	}

	public void copy(PointGridSpace p)
	{
		p.x = x;
		p.y = y;
	}

}
