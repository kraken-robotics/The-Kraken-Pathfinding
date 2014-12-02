package pathfinding;

import java.util.ArrayList;

import container.Service;
import smartMath.Vec2;
import table.obstacles.ObstacleManager;
import utils.Config;
import utils.Log;

/**
 * Classe encapsulant les calculs de pathfinding
 * @author pf
 *
 */

public class Pathfinding implements Service
{
	private static transient int nb_nodes = 2;
	private Log log;
	private Config config;
	
	private Vec2[] nodes = {new Vec2(100, 200), new Vec2(400, 800)};
	/**
	 * Contient le graphe des connexions avec les distances entre points
	 * Mettre -1 pour une distance infinie
	 */
	private double[][] isConnected = {{0, 0}, {0, 0}};
	
	/**
	 * Constructeur du système de recherche de chemin
	 */
	public Pathfinding(Log log, Config config)
	{
		this.log = log;
		this.config = config;

		for(int i = 0; i < nb_nodes; i++)
			for(int j = 0; j < nb_nodes; j++)
				if(isConnected[i][j] == 0)
				{
					isConnected[i][j] = nodes[i].distance(nodes[j]);
					isConnected[i][j] = isConnected[j][i];
				}
	}
	
	public ArrayList<Vec2> computePath(Vec2 orig, Vec2 dest, ObstacleManager obstaclemanager)
	{
		ArrayList<Vec2> out = new ArrayList<Vec2>();
		out.add(orig);
		
		// On commence d'abord par trouver le point de passage le plus
		// proche du point d'entrée
		Vec2 point_depart = nodes[0];
		float distance_min = orig.squaredDistance(nodes[0]);
		for(int i = 1; i < nb_nodes; i++)
		{
			float tmp = orig.squaredDistance(nodes[i]);
			if(tmp < distance_min)
			{
				distance_min = tmp;
				point_depart = nodes[i];
			}
		}
		
	}
	
	@Override
	public void updateConfig() {
		// TODO Auto-generated method stub
		
	}
}
