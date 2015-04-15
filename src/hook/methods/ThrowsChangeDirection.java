package hook.methods;

import hook.Executable;
import robot.RobotChrono;
import strategie.GameState;
import vec2.ReadWrite;
import exceptions.ChangeDirectionException;

/**
 * Lève une exception signifiant qu'on va changer de direction
 * @author pf
 *
 */

public class ThrowsChangeDirection implements Executable
{

	private ChangeDirectionException exception;
	
	@Override
	public void execute() throws ChangeDirectionException
	{
		if(exception == null)
			exception = new ChangeDirectionException();
		throw exception;
	}

	@Override
	public void updateGameState(GameState<RobotChrono,ReadWrite> state)
	{}

}
