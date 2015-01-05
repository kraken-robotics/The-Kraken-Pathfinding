package entryPoints.benchmarks;

import java.util.ArrayList;

import container.Container;
import container.ServiceNames;
import robot.RobotChrono;
import robot.RobotReal;
import strategie.GameState;
import utils.Config;
import utils.Log;
import astar.AStar;
import astar.arc.Decision;
import astar.arcmanager.StrategyArcManager;

// TODO: tester

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
	    	GameState<RobotChrono> chronostate = gamestate.cloneGameState();
	   		int nb_iter = 1;
			long date_avant = System.currentTimeMillis();
	    	for(int k = 0; k < nb_iter; k++)
	    	{
	    		ArrayList<Decision> decisions = astar.computeStrategyEmergency(chronostate, 90000);
	    		for(Decision d: decisions)
	    			log.appel_static(d);
	    	}
			log.appel_static("Durée moyenne en µs: "+1000*(System.currentTimeMillis()-date_avant)/nb_iter);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    }
	
}
