package hook.methods;

import robot.RobotChrono;
import strategie.GameState;
import exceptions.WallCollisionDetectedException;
import hook.Executable;

/**
 * Exception levée si on détecte qu'on va incessament rentrer dans un mur
 * @author pf
 *
 */

public class ThrowsWallCollisionDetectedException implements Executable
{
	private WallCollisionDetectedException exception;
	
	@Override
	public void execute() throws WallCollisionDetectedException
	{
		if(exception == null)
			exception = new WallCollisionDetectedException();
		throw exception;
	}

	@Override
	public void updateGameState(GameState<RobotChrono> state)
	{}

}
