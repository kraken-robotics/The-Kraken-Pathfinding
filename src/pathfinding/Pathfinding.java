package pathfinding;
import java.util.ArrayList;

import container.Service;
import smartMath.Vec2;
import table.Table;
import utils.Config;
import utils.Log;

/**
 * Classe encapsulant les calculs de pathfinding
 * @author pf
 *
 */

public class Pathfinding implements Service
{
	private static Vec2[] nodes = {new Vec2(100, 200), new Vec2(400, 800)};
	/**
	 * Contient le graphe des connexions avec les distances entre points
	 * Mettre -1 pour une distance infinie
	 */
	private static double[][] isConnected = {{0, 0}, {0, 0}};

	static {
		/** Initialisation static (et donc une fois pour toutes)
		 * des distances entre les points de passages
		 */
		for(int i = 0; i < 2; i++)
			for(int j = 0; j < 2; j++)
				if(isConnected[i][j] == 0)
				{
					isConnected[i][j] = nodes[i].distance(nodes[j]);
					isConnected[i][j] = isConnected[j][i];
				}
	}
	
	/**
	 * Constructeur du système de recherche de chemin
	 */
	public Pathfinding(Log log, Config config, Table table)
	{
		
	}
	
	public ArrayList<Vec2> computePath(Vec2 start, Vec2 end)
	{
		
		// TODO
		// voici un pathfinding math�matiquement d�montr� comme correct
		// correct au sens d'un chemin partant du d�part et allant a l'arriv�e
		
		// bon apr�s si vous chipottez pour les obstacles en chemin aussi...
		
		ArrayList<Vec2> out = new ArrayList<Vec2>();
		out.add(end);
		return out;
	}

	@Override
	public void updateConfig() {
		// TODO Auto-generated method stub
		
	}
}
