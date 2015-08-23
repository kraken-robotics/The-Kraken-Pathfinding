package pathfinding;

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
 * Utilisée uniquement pour le pathfinding de RobotReal
 * @author pf
 *
 */

public class GridSpace implements Service
{
	protected Log log;
	private ObstaclesIterator iterator;
	private boolean ignoreElementJeu;
	
	private static final int COEFF_HEURISTIQUE = 1;
	// soit 46.875 mm entre chaque point
	private static final int PRECISION = 6;
	public static final int NB_POINTS_POUR_TROIS_METRES = (1 << PRECISION);
	public static final int NB_POINTS_POUR_DEUX_METRES = (1 << PRECISION)*2/3;
	public static final double DISTANCE_ENTRE_DEUX_POINTS = 3000/NB_POINTS_POUR_TROIS_METRES;
	private static final int nbPoints = NB_POINTS_POUR_DEUX_METRES * NB_POINTS_POUR_TROIS_METRES;
	
	// les nœuds ont 8 voisins, mais par symétrie on n'a besoin que de 4 nombres
	// cette grille est constante, c'est-à-dire qu'elle ne contient que les obstacles fixes
	private boolean[] grille = new boolean[4*nbPoints];
	
	public GridSpace(Log log, ObstaclesMemory memory, Table table)
	{
		this.log = log;
//		this.table = table;
		iterator = new ObstaclesIterator(log, memory);
	}
	
	public void updateObstaclesMobiles()
	{
		iterator.reinitNow();
		
	}
	
	public double distanceHeuristique(int pointA, int pointB)
	{
		int dx = (pointA & (NB_POINTS_POUR_TROIS_METRES - 1)) - (pointB & (NB_POINTS_POUR_TROIS_METRES - 1)); // ceci est un modulo
		int dy = (pointA >> PRECISION) - (pointB >> PRECISION); // ceci est une division
		return COEFF_HEURISTIQUE * 1000 * Math.hypot(dx, dy);
	}

	public void updateElementsJeu()
	{
		if(ignoreElementJeu)
			return;
	}

	public int getGridPointVoisin(int point, int direction)
	{
		switch(direction)
		{
		case 0:
			return 4*point;
		case 4:
			return 4*point+1;
		case 2:
			return 4*point+2;
		case 6:
			return 4*point+3;
		case 7:
			return 4*point+7;
		case 3:
			return 4*point+4*NB_POINTS_POUR_TROIS_METRES-2;
		case 5:
			return 4*point+4*NB_POINTS_POUR_TROIS_METRES+1;
//		case 1:
		default:
			return 4*point+4*NB_POINTS_POUR_TROIS_METRES+4;
		}
		
	}

	@Override
	public void useConfig(Config config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateConfig(Config config) {
		// TODO Auto-generated method stub
		
	}

	public boolean isTraversable(int gridpoint, int direction)
	{
		return grille[getGridPointVoisin(gridpoint, direction)];
	}
	
	public int computeGridPoint(Vec2<ReadOnly> p)
	{
		return (int) (4*NB_POINTS_POUR_TROIS_METRES*(int) Math.round(p.y / GridSpace.DISTANCE_ENTRE_DEUX_POINTS) + Math.round(p.x / GridSpace.DISTANCE_ENTRE_DEUX_POINTS + GridSpace.NB_POINTS_POUR_TROIS_METRES / 2));
	}

}
