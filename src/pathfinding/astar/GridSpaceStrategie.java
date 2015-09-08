package pathfinding.astar;

import pathfinding.dstarlite.GridSpace;
import permissions.ReadOnly;
import permissions.ReadWrite;
import utils.Config;
import utils.Log;
import utils.Vec2;
import container.Service;

/**
 * GridSpace utilisé par la stratégie.
 * @author pf
 *
 */

public class GridSpaceStrategie implements Service
{
	protected Log log;
	
	/**
	 * Comme on veut que le DStarLite recherche plus de noeuds qu'il n'y en aurait besoin, ce coeff ne vaut pas 1
	 */
	private static final int COEFF_HEURISTIQUE = 2;

	public static final int PRECISION = 4;
	public static final int NB_POINTS_POUR_TROIS_METRES = (1 << PRECISION);
	public static final int NB_POINTS_POUR_DEUX_METRES = (int) ((1 << PRECISION)*2./3.);
	public static final double DISTANCE_ENTRE_DEUX_POINTS = 3000./NB_POINTS_POUR_TROIS_METRES;
	public static final int DISTANCE_ENTRE_DEUX_POINTS_1024 = (1024*3000)/NB_POINTS_POUR_TROIS_METRES;
	public static final int NB_POINTS = NB_POINTS_POUR_DEUX_METRES * NB_POINTS_POUR_TROIS_METRES;
	private static final int X_MAX = NB_POINTS_POUR_TROIS_METRES-1;
	private static final int Y_MAX = NB_POINTS_POUR_DEUX_METRES-1;

	public GridSpaceStrategie(Log log)
	{
		this.log = log;
	}
	
	/**
	 * On utilise la distance octile pour l'heuristique
	 * @param pointA
	 * @param pointB
	 * @return
	 */
	public final int distanceHeuristique(int pointA, int pointB)
	{
		int dx = Math.abs((pointA & (NB_POINTS_POUR_TROIS_METRES - 1)) - (pointB & (NB_POINTS_POUR_TROIS_METRES - 1))); // ceci est un modulo
		int dy = Math.abs((pointA >> PRECISION) - (pointB >> PRECISION)); // ceci est une division
		return COEFF_HEURISTIQUE * (1000 * Math.max(dx, dy) + 414 * Math.min(dx, dy));
	}
	
	@Override
	public void useConfig(Config config)
	{}

	@Override
	public void updateConfig(Config config)
	{}


	public int getGridPointVoisin(int point, int direction)
	{
		int x = point & (NB_POINTS_POUR_TROIS_METRES - 1);
		int y = point >> PRECISION;

		switch(direction)
		{
		case 0:
//			NO
			if(x > 0 && y < Y_MAX)
				return point+NB_POINTS_POUR_TROIS_METRES-1;
			return -1; // hors table

		case 1:
//			SE
			if(x < X_MAX && y > 0)
				return point-NB_POINTS_POUR_TROIS_METRES+1;
			return -1; // hors table

		case 2:
//			NE
			if(x < X_MAX && y < Y_MAX)
				return point+NB_POINTS_POUR_TROIS_METRES+1;
			return -1; // hors table

		case 3:
//			SO
			if(x > 0 && y > 0)
				return point-NB_POINTS_POUR_TROIS_METRES-1;
			return -1; // hors table

		case 4:
//			N
			if(y < Y_MAX)
				return point+NB_POINTS_POUR_TROIS_METRES;
			return -1; // hors table

		case 5:
//			S
			if(y > 0)
				return point-NB_POINTS_POUR_TROIS_METRES;
			return -1; // hors table

		case 6:
//			O
			if(x > 0)
				return point-1;
			return -1; // hors table

//		case 7:
		default:
//			E
			if(x < X_MAX)
				return point+1;
			return -1; // hors table
		}
		
	}
	
	/**
	 * Renvoie l'indice du gridpoint le plus proche de cette position
	 * @param p
	 * @return
	 */
	public int computeGridPoint(Vec2<ReadOnly> p)
	{
		return (int) (NB_POINTS_POUR_TROIS_METRES*(int) Math.round(p.y / GridSpace.DISTANCE_ENTRE_DEUX_POINTS) + Math.round((p.x+1500) / GridSpace.DISTANCE_ENTRE_DEUX_POINTS));
	}

	public Vec2<ReadOnly> computeVec2(int gridpoint)
	{
		Vec2<ReadWrite> out = new Vec2<ReadWrite>();
		computeVec2(out, gridpoint);
		return out.getReadOnly();
	}

	public void computeVec2(Vec2<ReadWrite> v, int gridpoint)
	{
		v.x = (((gridpoint & (NB_POINTS_POUR_TROIS_METRES - 1)) * GridSpace.DISTANCE_ENTRE_DEUX_POINTS_1024) >> 10) - 1500;
		v.y = ((gridpoint >> PRECISION) * GridSpace.DISTANCE_ENTRE_DEUX_POINTS_1024) >> 10;
	}

}
