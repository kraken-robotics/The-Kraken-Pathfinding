package pathfinding;

import obstacles.ObstaclesIterator;
import obstacles.ObstaclesMemory;
import table.Table;
import utils.Config;
import utils.Log;
import container.Service;

/**
 * La classe qui contient la grille utilisée par le pathfinding.
 * Utilisée uniquement pour le pathfinding de RobotReal
 * @author pf
 *
 */

public class GridSpace implements Service
{
	private Log log;
	private ObstaclesIterator iterator;
	private Table table;
	private boolean ignoreElementJeu;
	
	// soit 50 mm entre chaque point
	public static final int NB_POINTS_PAR_METRE = 20;
	public static final int DISTANCE_ENTRE_DEUX_POINTS = 1000/NB_POINTS_PAR_METRE;
	private static final int nbPoints = 2*NB_POINTS_PAR_METRE * 3*NB_POINTS_PAR_METRE;
	
	// les nœuds ont 8 voisins, mais par symétrie on n'a besoin que de 4 nombres
	// cette grille est constante, c'est-à-dire qu'elle ne contient que les obstacles fixes
	private boolean[] grille = new boolean[4*nbPoints];
	
	public GridSpace(Log log, ObstaclesMemory memory, Table table)
	{
		this.log = log;
		this.table = table;
		iterator = new ObstaclesIterator(log, memory);
	}
	
	public void updateObstaclesMobiles()
	{
		iterator.reinitNow();
		
	}

	public void updateElementsJeu()
	{
		if(ignoreElementJeu)
			return;
	}

	private int getIndiceGrille(int point, DirectionGridSpace direction)
	{
		switch(direction)
		{
		case SO:
			return 4*point;
		case S:
			return 4*point+1;
		case SE:
			return 4*point+2;
		case O:
			return 4*point+3;
		case E:
			return 4*point+7;
		case NO:
			return 4*point+4*3*NB_POINTS_PAR_METRE-2;
		case N:
			return 4*point+4*3*NB_POINTS_PAR_METRE+1;
//		case NE:
		default:
			return 4*point+4*3*NB_POINTS_PAR_METRE+4;
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
}
