package pathfinding.dstarlite;

import obstacles.ObstaclesIterator;
import obstacles.ObstaclesMemory;
import permissions.ReadOnly;
import table.Table;
import utils.Config;
import utils.Log;
import utils.Vec2;
import container.Service;

/**
 * La classe qui contient la grille utilisée par le pathfinding.
 * Utilisée uniquement pour le pathfinding de RobotReal.
 * Ordre des directions : NO, SE, NE, SO, N, S, O, E;
 * Ordre dans la grille : NO, NE, N, O;
 * @author pf
 *
 */

public class GridSpace implements Service
{
	protected Log log;
	private ObstaclesIterator iterator;
	private boolean ignoreElementJeu;
	
	private static final int COEFF_HEURISTIQUE = 1;
	public static final int PRECISION = 6;
	public static final int NB_POINTS_POUR_TROIS_METRES = (1 << PRECISION);
	public static final int NB_POINTS_POUR_DEUX_METRES = (int) ((1 << PRECISION)*2./3.);
	public static final double DISTANCE_ENTRE_DEUX_POINTS = 3000./NB_POINTS_POUR_TROIS_METRES;
	public static final int NB_POINTS = NB_POINTS_POUR_DEUX_METRES * NB_POINTS_POUR_TROIS_METRES;
	private static final int X_MAX = NB_POINTS_POUR_TROIS_METRES-1;
	private static final int Y_MAX = NB_POINTS_POUR_DEUX_METRES-1;
	
	// les nœuds ont 8 voisins, mais par symétrie on n'a besoin que de 4 nombres
	// cette grille est constante, c'est-à-dire qu'elle ne contient que les obstacles fixes
	private boolean[] grille = new boolean[4*NB_POINTS];
	
	public GridSpace(Log log, ObstaclesMemory memory, Table table)
	{
		this.log = log;
//		this.table = table;
		iterator = new ObstaclesIterator(log, memory);

		for(int i = 0; i < 4*NB_POINTS; i++)
			grille[i] = true;

	}
	
	public void updateObstaclesMobiles()
	{
		iterator.reinitNow();
		
	}
	
	/**
	 * On utilise la distance euclidienne pour l'heuristique
	 * @param pointA
	 * @param pointB
	 * @return
	 */
	public int distanceHeuristique(int pointA, int pointB)
	{
		int dx = (pointA & (NB_POINTS_POUR_TROIS_METRES - 1)) - (pointB & (NB_POINTS_POUR_TROIS_METRES - 1)); // ceci est un modulo
		int dy = (pointA >> PRECISION) - (pointB >> PRECISION); // ceci est une division
		return (int) Math.round(COEFF_HEURISTIQUE * 1000 * Math.hypot(dx, dy));
	}

	public void updateElementsJeu()
	{
		if(ignoreElementJeu)
			return;
	}

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

	@Override
	public void useConfig(Config config)
	{}

	@Override
	public void updateConfig(Config config)
	{}

	/**
	 * Signale si on peut passer d'un point à un de ses voisins.
	 * On suppose que ce voisin n'est pas hors table.
	 * @param gridpoint
	 * @param direction
	 * @return
	 */
	private boolean isTraversable(int gridpoint, int direction)
	{
		if((direction & 1) == 0)
			return grille[4*gridpoint+direction/2];
		else
			return grille[4*getGridPointVoisin(gridpoint, direction)+direction/2];
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

	/**
	 * Renvoie la distance en fonction de la direction
	 * @param i
	 * @return
	 */
	public int distance(int point, int i) {
		if(!isTraversable(point, i))
			return Integer.MAX_VALUE;
		if(i < 4)
			return 1414;
		else
			return 1000;
	}

	public Vec2<ReadOnly> computeVec2(int gridpoint)
	{
		return new Vec2<ReadOnly>((int) Math.round((gridpoint & (NB_POINTS_POUR_TROIS_METRES - 1)) * GridSpace.DISTANCE_ENTRE_DEUX_POINTS - 1500),
				(int) Math.round((gridpoint >> PRECISION) * GridSpace.DISTANCE_ENTRE_DEUX_POINTS));
	}

	public boolean isUrgent() {
		// TODO Auto-generated method stub
		return false;
	}

}
