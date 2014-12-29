package hook.methods;

import robot.RobotChrono;
import strategie.GameState;
import exceptions.FinMatchException;
import hook.Executable;

/**
 * Lève une exception une fois le match fini
 * @author pf
 *
 */

public class FinMatchCheck implements Executable {

	@Override
	public void execute() throws FinMatchException
	{
		throw new FinMatchException();
	}

	@Override
	public void updateGameState(GameState<RobotChrono> state)
	{}

}
