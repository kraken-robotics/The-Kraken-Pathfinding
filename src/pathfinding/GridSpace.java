package pathfinding;

import container.Service;
import smartMath.Vec2;
import table.obstacles.ObstacleManager;
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
	
	ObstacleManager obstaclemanager;
		
	private static Vec2[] nodes = {new Vec2(100, 200), new Vec2(400, 800)};

	// Constante bien utile
	private static final int NB_NODES = nodes.length;

	// TODO: à calculer
	private NodesConnection[][] isConnected = {{NodesConnection.POSSIBLE, NodesConnection.POSSIBLE}, {NodesConnection.POSSIBLE, NodesConnection.POSSIBLE}};
	
	// Contient les distances entre chaque point de passage
	private double[][] distances = new double[NB_NODES][NB_NODES];

	public GridSpace(Log log, Config config, ObstacleManager obstaclemanager)
	{
		this.obstaclemanager = obstaclemanager;
		
		// TODO calculer isConnected avec calculs de collision
		for(int i = 0; i < NB_NODES; i++)
			for(int j = 0; j < i; j++)
				if(isConnected[i][j] != NodesConnection.ALWAYS_IMPOSSIBLE)
				{
					distances[i][j] = nodes[i].distance(nodes[j]);
					distances[i][j] = distances[j][i];
				}
	}
	
	/**
	 * Réinitialise l'état des liaisons.
	 * A faire quand les obstacles mobiles ont changé.
	 */
	public void reinitConnections()
	{
		for(int i = 0; i < NB_NODES; i++)
			for(int j = 0; j < i; j++)
				if(isConnected[i][j] != NodesConnection.ALWAYS_IMPOSSIBLE)
				{
					isConnected[i][j] = NodesConnection.UNKNOW;
					isConnected[j][i] = NodesConnection.UNKNOW;
				}
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

}
