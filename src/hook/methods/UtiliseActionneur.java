package hook.methods;

import java.util.ArrayList;

import permissions.ReadWrite;
import robot.ActuatorOrder;
import robot.RobotChrono;
import strategie.GameState;
import hook.Executable;

/**
 * Utilise un actionneur
 * @author pf
 *
 */

public class UtiliseActionneur implements Executable
{
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
	public ArrayList<String> toSerial()
	{
		ArrayList<String> out = new ArrayList<String>();
		out.add("act");
		out.add(o.getSerialOrder());
		return out;
	}

}
