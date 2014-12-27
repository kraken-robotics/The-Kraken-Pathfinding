package benchmarks;
import java.util.Random;

import container.Container;
import pathfinding.AStar;
import pathfinding.PathfindingArcManager;
import robot.RobotChrono;
import robot.RobotReal;
import strategie.GameState;
import utils.Log;
import enums.PathfindingNodes;
import enums.ServiceNames;

public class BenchmarkPathfinding {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		try {
			Container container = new Container();
			
			Log log = (Log)container.getService(ServiceNames.LOG);
			AStar<PathfindingArcManager, PathfindingNodes> pathfinding = (AStar<PathfindingArcManager, PathfindingNodes>) container.getService(ServiceNames.A_STAR_PATHFINDING);
			GameState<RobotReal> state = (GameState<RobotReal>)container.getService(ServiceNames.REAL_GAME_STATE);
			GameState<RobotChrono> state_chrono = state.cloneGameState();
			
			Random randomgenerator = new Random();
			int nb_iter = 500000;
			long date_avant = 0;
			for(int k = 0; k < nb_iter; k++)
			{
				if(k == nb_iter/2)
					date_avant = System.currentTimeMillis();
				PathfindingNodes i = PathfindingNodes.values[randomgenerator.nextInt(PathfindingNodes.length)];
				PathfindingNodes j = PathfindingNodes.values[randomgenerator.nextInt(PathfindingNodes.length)];
				state_chrono.robot.setPositionPathfinding(i);
				pathfinding.computePath(state_chrono, j, true);
			}
			log.appel_static("Durée moyenne en µs: "+2*1000*(System.currentTimeMillis()-date_avant)/nb_iter);
			container.destructor();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
