package entryPoints.benchmarks;

import container.Container;
import container.ServiceNames;
import robot.RobotChrono;
import robot.RobotReal;
import strategie.GameState;
import utils.Log;
import vec2.ReadWrite;

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
			GameState<RobotReal,ReadWrite> state = (GameState<RobotReal,ReadWrite>)container.getService(ServiceNames.REAL_GAME_STATE);
			GameState<RobotChrono,ReadWrite> gamestate = GameState.cloneGameState(state.getReadOnly());
			GameState<RobotChrono,ReadWrite> gamestate2 = GameState.cloneGameState(state.getReadOnly());
			long date_avant = System.currentTimeMillis();
			long nb_iter = 20000000l;
			for(long i = 0; i < nb_iter; i++)
				GameState.copy(gamestate2.getReadOnly(), gamestate);

			log.debug("Durée totale en ms: "+(System.currentTimeMillis()-date_avant));
			container.destructor();
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
