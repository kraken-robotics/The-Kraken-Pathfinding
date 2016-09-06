package pathfinding.dstarlite;

import utils.Config;
import utils.Vec2;
import utils.permissions.ReadOnly;
import utils.permissions.ReadWrite;
import container.Service;

/**
 * Un point du gridspace
 * @author pf
 *
 */

public class PointGridSpace implements Service
{
	public static final int PRECISION = 6;
	public static final int NB_POINTS_POUR_TROIS_METRES = (1 << PRECISION);
	public static final int NB_POINTS_POUR_DEUX_METRES = (int) ((1 << PRECISION)*2./3.)+1;
	public static final double DISTANCE_ENTRE_DEUX_POINTS = 3000./(NB_POINTS_POUR_TROIS_METRES-1);
	public static final int DISTANCE_ENTRE_DEUX_POINTS_1024 = (int)(1024*3000./(NB_POINTS_POUR_TROIS_METRES-1));
	public static final int NB_POINTS = NB_POINTS_POUR_DEUX_METRES * NB_POINTS_POUR_TROIS_METRES;
	public static final int X_MAX = NB_POINTS_POUR_TROIS_METRES - 1;
	public static final int Y_MAX = NB_POINTS_POUR_DEUX_METRES - 1;

	/**
	 * Attention ! Le repère de ce x,y est celui pour lequel x et y sont toujours positifs
	 */
	public final int x, y, hashcode;
	
	/**
	 * Construit à partir du hashCode
	 * @param i
	 */
	PointGridSpace(int i)
	{
		y = i >> PRECISION;
		x = i & (NB_POINTS_POUR_TROIS_METRES - 1);
		hashcode = i;
	}

	@Override
	public int hashCode()
	{
		return hashcode;
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

	public static final Vec2<ReadOnly> computeVec2(PointGridSpace gridpoint)
	{
		Vec2<ReadWrite> out = new Vec2<ReadWrite>(((gridpoint.x * DISTANCE_ENTRE_DEUX_POINTS_1024) >> 10) - 1500, (gridpoint.y * DISTANCE_ENTRE_DEUX_POINTS_1024) >> 10);
		return out.getReadOnly();
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
