package hook.methods;

import pathfinding.GameState;
import permissions.ReadWrite;
import robot.ActuatorOrder;
import robot.RobotChrono;
import hook.Executable;

/**
 * Utilise un actionneur
 * @author pf
 *
 */

public class UtiliseActionneur implements Executable
{
	//TODO attention à la symétrie des actionneurs
	private ActuatorOrder o;
	
	public UtiliseActionneur(ActuatorOrder o)
	{
		this.o = o;
	}
	
	@Override
	public void execute()
	{}

	@Override
	public void updateGameState(GameState<RobotChrono,ReadWrite> state)
	{}
	
	@Override
	public String toSerial()
	{
		return "act "+o.ordinal();
	}

}
