package entryPoints.benchmarks;

import container.Container;
import container.ServiceNames;
import obstacles.gameElement.GameElementNames;
import robot.RobotChrono;
import robot.RobotReal;
import strategie.GameState;
import utils.Vec2;
import enums.Tribool;

public class BenchmarkMemoryManager
{

	@SuppressWarnings("unchecked")
	public static void main(String[] args)
	{
		try {
		Container container = new Container();
		
		GameState<RobotReal> state = (GameState<RobotReal>)container.getService(ServiceNames.REAL_GAME_STATE);
		GameState<RobotChrono> gamestate = state.cloneGameState();
		GameState<RobotChrono> gamestate2 = state.cloneGameState();
		for(int i = 0; i < 100000; i++)
		{
			gamestate.robot.avancer(200);
			gamestate.gridspace.creer_obstacle(new Vec2(156, 282));
			gamestate.gridspace.setDone(GameElementNames.CLAP_3, Tribool.TRUE);
			gamestate2.copy(gamestate);
		}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
