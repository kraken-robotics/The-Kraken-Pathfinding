package pathfinding;

import robot.RobotChrono;
import strategie.GameState;
import utils.Log;
import utils.Config;
import container.Service;

// TODO

public class StrategyArcManager implements Service, ArcManager {

	public StrategyArcManager(Log log, Config config)
	{
	}

	@Override
	public void reinitIterator(GameState<RobotChrono> gamestate) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasNext(GameState<RobotChrono> state) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Arc next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double distanceTo(GameState<RobotChrono> state, Arc arc) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double heuristicCost(GameState<RobotChrono> state1,
			GameState<RobotChrono> state2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getHash(GameState<RobotChrono> state) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void updateConfig() {
		// TODO Auto-generated method stub
		
	}

}
