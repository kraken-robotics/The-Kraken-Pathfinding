package entryPoints.benchmarks;
import java.util.Random;

import astar.AStar;
import astar.arc.PathfindingNodes;
import astar.arc.SegmentTrajectoireCourbe;
import astar.arcmanager.PathfindingArcManager;
import container.Container;
import container.ServiceNames;
import robot.RobotChrono;
import robot.RobotReal;
import strategie.GameState;
import utils.Log;
import vec2.ReadWrite;

/**
 * Benchmark de la recherche de chemin. Utilisé pour l'optimisation.
 * @author pf
 *
 */

public class BenchmarkPathfinding {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		try {
			Container container = new Container();
			
			Log log = (Log)container.getService(ServiceNames.LOG);
			AStar<PathfindingArcManager, SegmentTrajectoireCourbe> pathfinding = (AStar<PathfindingArcManager, SegmentTrajectoireCourbe>) container.getService(ServiceNames.A_STAR_PATHFINDING);
			GameState<RobotReal,ReadWrite> state = (GameState<RobotReal,ReadWrite>)container.getService(ServiceNames.REAL_GAME_STATE);
			GameState<RobotChrono,ReadWrite> state_chrono = GameState.cloneGameState(state.getReadOnly());
			
			Random randomgenerator = new Random();
			int nb_iter = 500000;
			long date_avant = 0;
			for(int k = 0; k < nb_iter; k++)
			{
				if(k == nb_iter/2)
					date_avant = System.currentTimeMillis();
				PathfindingNodes i = PathfindingNodes.values[randomgenerator.nextInt(PathfindingNodes.length)];
				PathfindingNodes j = PathfindingNodes.values[randomgenerator.nextInt(PathfindingNodes.length)];
				GameState.setPositionPathfinding(state_chrono,i);
				pathfinding.computePath(state_chrono, j, true);
			}
			log.debug("Durée moyenne en µs: "+2*1000*(System.currentTimeMillis()-date_avant)/nb_iter);
			container.destructor();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
