package pathfinding.lpastar;

import java.util.ArrayList;

import pathfinding.astar.AStar;
import permissions.ReadWrite;
import robot.RobotReal;
import strategie.GameState;
import utils.Config;
import utils.Log;
import container.Service;

/**
 * Recherche de stratégie avec un LPA*
 * Permet la replannification rapide. Par contre, comme on a besoin de la date
 * à chaque noeud, on doit commencer du point de départ et pas de l'arrivée,
 * ce qui empêche de faire un D* Lite.
 * @author pf
 *
 */

public class LPAStar implements Service
{
	private GameState<RobotReal,ReadWrite> state;
	private AStar pathfinding;
	private Log log;
	
	public LPAStar(Log log, GameState<RobotReal,ReadWrite> state, AStar pathfinding)
	{
		this.log = log;
		this.state = state;
		this.pathfinding = pathfinding;
	}
	
	/**
	 * Calcule une stratégie.
	 * Peut être appelé plusieurs fois si on prend du retard.
	 */
	public ArrayList<StrategieArc> computeStrategy()
	{
		return null;
		
	}
	
	public ArrayList<StrategieArc> updateStrategy()
	{
		return null;
	}
	
	private void computeShortestPath()
	{
		
	}
	
	@Override
	public void updateConfig(Config config)
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void useConfig(Config config)
	{
		// TODO Auto-generated method stub
		
	}

}
