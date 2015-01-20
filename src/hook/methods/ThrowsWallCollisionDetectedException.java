package hook.methods;

import robot.RobotChrono;
import strategie.GameState;
import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import exceptions.WallCollisionDetectedException;
import hook.Executable;

public class ThrowsWallCollisionDetectedException implements Executable
{
	WallCollisionDetectedException exception;
	@Override
	public void execute() throws FinMatchException, ScriptHookException, WallCollisionDetectedException
	{
		if(exception == null)
			exception = new WallCollisionDetectedException();
		throw exception;
	}

	@Override
	public void updateGameState(GameState<RobotChrono> state)
	{}

}
