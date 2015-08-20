package table;

import buffer.DataForSerialOutput;
import strategie.StrategieNotifieur;
import utils.Config;
import utils.Log;
import container.Service;

/**
 * La classe qui contient la grille utilisée par le pathfinding.
 * S'occupe d'intégrer les modifications provenant d'ObstacleManager.
 * @author pf
 *
 */

public class GridSpace implements Service
{
	private Log log;
	private ObstacleManager obstaclemanager;
	private StrategieNotifieur notifieur;
	private DataForSerialOutput serie;
	
	// 2^PRECISION points dans 1000 mm
	private static final int PRECISION = 4;
	private static final int NB_POINTS_PAR_METRE = (1 << PRECISION);
	private static final int nbPoints = 2*NB_POINTS_PAR_METRE * 3*NB_POINTS_PAR_METRE;
	
	// les nœuds ont 8 voisins, mais par symétrie on n'a besoin que de 4 nombres
	private boolean[] grille = new boolean[4*nbPoints];
	
	public GridSpace(Log log, ObstacleManager obstaclemanager, StrategieNotifieur notifieur, DataForSerialOutput serie)
	{
		this.log = log;
		this.obstaclemanager = obstaclemanager;
		this.notifieur = notifieur;
		this.serie = serie;
	}
	
	@Override
	public void updateConfig(Config config) {
		// TODO Auto-generated method stub
		
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

	public GridSpace clone(long tempsDepuisDebut) {
		// TODO Auto-generated method stub
		return this;
	}

	public void copy(GridSpace gridspace, long tempsDepuisDebutMatch) {
		// TODO Auto-generated method stub
		
	}

}
