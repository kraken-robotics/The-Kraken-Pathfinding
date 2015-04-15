package entryPoints.benchmarks;

import container.Container;
import container.ServiceNames;
import robot.RobotChrono;
import robot.RobotReal;
import strategie.GameState;
import utils.Config;
import utils.Log;
import vec2.ReadOnly;
import vec2.Vec2;
import astar.AStar;
import astar.arc.Decision;
import astar.arcmanager.StrategyArcManager;

/**
 * Benchmark de la stratégie. Utilisé pour l'optimisation.
 * @author pf
 *
 */

public class BenchmarkStrategy {

	@SuppressWarnings("unchecked")
	public static void main(String[] args)
    {
		try {
			Container container = new Container();
			
			Log log = (Log)container.getService(ServiceNames.LOG);
			Config config = (Config)container.getService(ServiceNames.CONFIG);
			AStar<StrategyArcManager, Decision> astar = (AStar<StrategyArcManager, Decision>) container.getService(ServiceNames.A_STAR_STRATEGY);
			GameState<RobotReal> gamestate = (GameState<RobotReal>)container.getService(ServiceNames.REAL_GAME_STATE);
	    	config.setDateDebutMatch();
	    	gamestate.robot.setPosition(new Vec2<ReadOnly>(600, 1000));
	    	GameState<RobotChrono> chronostate = gamestate.cloneGameState();
	    	
	   		int nb_iter = 100000;
			long date_avant = System.currentTimeMillis();
	    	for(int k = 0; k < nb_iter; k++)
	    		astar.computeStrategyEmergency(chronostate, 90000);
			log.debug("Durée moyenne en µs: "+1000*(System.currentTimeMillis()-date_avant)/nb_iter);
			container.destructor();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    }
	
}
