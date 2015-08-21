package pathfinding;

import java.util.ArrayList;

import permissions.ReadOnly;
import robot.RobotReal;
import strategie.GameState;
import utils.Config;
import utils.Log;
import utils.Vec2;
import container.Service;

/**
 * Recherche de chemin avec replanification rapide.
 * @author pf
 *
 */

public class DStarLite implements Service
{
	private Log log;
	private GridSpace gridspace;
	
	public DStarLite(Log log, GridSpace gridspace)
	{
		this.log = log;
		this.gridspace = gridspace;
	}
	
	@Override
	public void updateConfig(Config config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void useConfig(Config config) {
		// TODO Auto-generated method stub
		
	}

	public ArrayList<Vec2<ReadOnly>> computeShortestPath()
	{
		return null;
	}
	
	public ArrayList<Vec2<ReadOnly>> updatePath() {
		// TODO Auto-generated method stub
		return null;
	}

}
