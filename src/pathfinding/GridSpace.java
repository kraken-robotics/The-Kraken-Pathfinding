package pathfinding;

import obstacles.ObstacleManager;
import container.Service;
import smartMath.Vec2;
import utils.Config;
import utils.Log;
import enums.NodesConnection;

/**
 * Contient les informations sur le graphe utilisé par le pathfinding.
 * Intègre un mécanisme de cache afin d'accélérer les calculs.
 * @author pf
 *
 */

public class GridSpace implements Service {
	
	private Log log;
	private Config config;
	private ObstacleManager obstaclemanager;
		
	private static Vec2[] nodes = {new Vec2(100, 200), new Vec2(400, 800)};

	// Constante bien utile
	private static final int NB_NODES = nodes.length;

	// Rempli de ALWAYS_IMPOSSIBLE et UNKNOW. Ne change pas.
	private static NodesConnection[][] isConnectedModel = null;

	// Dynamique.
	private NodesConnection[][] isConnected = new NodesConnection[NB_NODES][NB_NODES];
	
	// Contient les distances entre chaque point de passage
	private static double[][] distances = new double[NB_NODES][NB_NODES];

	public GridSpace(Log log, Config config, ObstacleManager obstaclemanager)
	{
		this.log = log;
		this.config = config;
		this.obstaclemanager = obstaclemanager;
		
		// Il est très important de ne faire ce long calcul qu'une seule fois,
		// à la première initialisation
		if(isConnectedModel == null)
			initStatic();
	}
	
	private void initStatic()
	{
		isConnectedModel = new NodesConnection[NB_NODES][NB_NODES];
		for(int i = 0; i < NB_NODES; i++)
			for(int j = 0; j < i; j++)
			{
				if(obstaclemanager.obstacle_fixe_dans_segment(nodes[i], nodes[j]))
					isConnectedModel[i][j] = NodesConnection.ALWAYS_IMPOSSIBLE;
				else
					isConnectedModel[i][j] = NodesConnection.UNKNOW;
				isConnectedModel[j][i] = isConnectedModel[i][j];
			}				
	
		for(int i = 0; i < NB_NODES; i++)
			for(int j = 0; j < i; j++)
				if(isConnectedModel[i][j] != NodesConnection.ALWAYS_IMPOSSIBLE)
				{
					distances[i][j] = nodes[i].distance(nodes[j]);
					distances[i][j] = distances[j][i];
				}
	}
	
	/**
	 * Réinitialise l'état des liaisons.
	 * A faire quand les obstacles mobiles ont changé.
	 */
	public void reinitConnections(long date)
	{
		obstaclemanager.supprimerObstaclesPerimes(date);
		for(int i = 0; i < NB_NODES; i++)
			for(int j = 0; j < NB_NODES; j++)
				isConnected[i][j] = isConnectedModel[i][j];
	}

	/**
	 * Surcouche de isConnected qui gère le cache
	 * @return
	 */
	public NodesConnection getIsConnected(int i, int j)
	{
		if(isConnected[i][j] != NodesConnection.UNKNOW)
			return isConnected[i][j];
		else if(obstaclemanager.obstacle_proximite_dans_segment(nodes[i], nodes[j]))
			isConnected[i][j] = NodesConnection.TMP_IMPOSSIBLE;
		else
			isConnected[i][j] = NodesConnection.POSSIBLE;
		isConnected[j][i] = isConnected[i][j];
		return isConnected[i][j];		
	}
	
	/**
	 * Retourne le point de passage le plus proche et accessible en ligne droite
	 * Attention, peut renvoyer "null" si aucun point de passage n'est atteignable en ligne droite.
	 * @param point
	 * @return
	 */
	public Vec2 nearestReachableNode(Vec2 point)
	{
		Vec2 point_depart = null;
		float distance_min = Float.MAX_VALUE;
		for(int i = 0; i < NB_NODES; i++)
		{
			float tmp = point.squaredDistance(nodes[i]);
			if(tmp < distance_min && !obstaclemanager.obstacle_proximite_dans_segment(point, nodes[i]))
			{
				distance_min = tmp;
				point_depart = nodes[i];
			}
		}
		return point_depart;
	}
	
	@Override
	public void updateConfig() {
		// TODO Auto-generated method stub
		
	}

	public void copy(GridSpace other, long date)
	{
		obstaclemanager.copy(other.obstaclemanager);
		other.reinitConnections(date);
	}
	
	public GridSpace clone(long date)
	{
		GridSpace cloned_gridspace = new GridSpace(log, config, obstaclemanager);
		copy(cloned_gridspace, date);
		return cloned_gridspace;
	}
    
	/**
	 * Utilisé uniquement pour les tests
	 * @return
	 */
    public int nbObstaclesMobiles()
    {
    	return obstaclemanager.nbObstaclesMobiles();
    }
    

}
