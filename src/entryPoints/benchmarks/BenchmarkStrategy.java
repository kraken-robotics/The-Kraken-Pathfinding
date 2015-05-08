package entryPoints.benchmarks;

import permissions.ReadOnly;
import permissions.ReadWrite;
import planification.astar.AStar;
import planification.astar.arc.Decision;
import planification.astar.arcmanager.StrategyArcManager;
import container.Container;
import container.ServiceNames;
import robot.RobotChrono;
import robot.RobotReal;
import strategie.GameState;
import utils.Config;
import utils.Log;
import utils.Vec2;

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
			GameState<RobotReal,ReadWrite> gamestate = (GameState<RobotReal,ReadWrite>)container.getService(ServiceNames.REAL_GAME_STATE);
	    	config.setDateDebutMatch();
	    	GameState.setPosition(gamestate, new Vec2<ReadOnly>(600, 1000));
	    	GameState<RobotChrono,ReadWrite> chronostate = GameState.cloneGameState(gamestate.getReadOnly());
	    	
	   		int nb_iter = 100000;
			long date_avant = System.currentTimeMillis();
	    	for(int k = 0; k < nb_iter; k++)
	    		astar.computeStrategyEmergency(chronostate.getReadOnly(), 90000);
			log.debug("Durée moyenne en µs: "+1000*(System.currentTimeMillis()-date_avant)/nb_iter);
			container.destructor();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    }
	
}
