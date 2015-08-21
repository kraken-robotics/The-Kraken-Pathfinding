package pathfinding;

import obstacles.ObstaclesMobilesIterator;
import obstacles.ObstaclesMobilesMemory;
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
	private ObstaclesMobilesIterator iterator;
	private Table table;
	private boolean ignoreElementJeu;
	
	// 2^PRECISION points dans 1000 mm
	private static final int PRECISION = 4;
	private static final int NB_POINTS_PAR_METRE = (1 << PRECISION);
	private static final int nbPoints = 2*NB_POINTS_PAR_METRE * 3*NB_POINTS_PAR_METRE;
	
	// les nœuds ont 8 voisins, mais par symétrie on n'a besoin que de 4 nombres
	// cette grille est constante, c'est-à-dire qu'elle ne contient que les obstacles fixes
	private boolean[] grille = new boolean[4*nbPoints];
	
	public GridSpace(Log log, ObstaclesMobilesMemory memory, Table table)
	{
		this.log = log;
		this.table = table;
		iterator = new ObstaclesMobilesIterator(log, memory);
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
		case NO:
			return 4*point;
		case N:
			return 4*point+1;
		case NE:
			return 4*point+2;
		case O:
			return 4*point+3;
		case E:
			return 4*point+7;
		case SO:
			return 4*point+4*3*NB_POINTS_PAR_METRE-2;
		case S:
			return 4*point+4*3*NB_POINTS_PAR_METRE+1;
//		case SE:
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
