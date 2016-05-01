package hook.methods;

import pathfinding.ChronoGameState;
import robot.actuator.ActuatorOrder;

import java.util.ArrayList;

import enums.SerialProtocol;
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
	public void updateGameState(ChronoGameState state)
	{}
	
	@Override
	public ArrayList<Byte> toSerial()
	{
		ArrayList<Byte> out = new ArrayList<Byte>();
		out.add((byte)(SerialProtocol.CALLBACK_AX12.code+o.id));
		out.add((byte) (o.angle >> 8));
		out.add((byte)o.angle);
		return out;
	}

}
