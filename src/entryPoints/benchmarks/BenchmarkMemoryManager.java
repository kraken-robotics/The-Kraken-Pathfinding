package entryPoints.benchmarks;

import container.Container;
import container.ServiceNames;
import robot.RobotChrono;
import robot.RobotReal;
import strategie.GameState;
import utils.Log;

/**
 * Benchmark sur memory manager. Utilisé pour l'optimisation.
 * @author pf
 *
 */

public class BenchmarkMemoryManager
{

	@SuppressWarnings("unchecked")
	public static void main(String[] args)
	{
		try {
			Container container = new Container();
			
			Log log = (Log)container.getService(ServiceNames.LOG);
			GameState<RobotReal> state = (GameState<RobotReal>)container.getService(ServiceNames.REAL_GAME_STATE);
			GameState<RobotChrono> gamestate = state.cloneGameState();
			GameState<RobotChrono> gamestate2 = state.cloneGameState();
			long date_avant = System.currentTimeMillis();
			long nb_iter = 20000000l;
			for(long i = 0; i < nb_iter; i++)
				gamestate2.copy(gamestate);

			log.debug("Durée totale en ms: "+(System.currentTimeMillis()-date_avant));
			container.destructor();
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
