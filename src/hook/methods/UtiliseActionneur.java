package hook.methods;

import pathfinding.GameState;
import permissions.ReadWrite;
import robot.ActuatorOrder;
import robot.RobotChrono;

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
	public ArrayList<Byte> toSerial()
	{
		// TODO
		ArrayList<Byte> out = new ArrayList<Byte>();
		out.add((byte)(SerialProtocol.CALLBACK_AX12.nb+0));
		out.add((byte)0);
		out.add((byte)0);
		return out;
	}

}
