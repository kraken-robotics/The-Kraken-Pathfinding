package hook.methods;

import pathfinding.GameState;
import permissions.ReadWrite;
import robot.RobotChrono;
import scripts.ScriptHookNames;

import java.util.ArrayList;

import enums.SerialProtocol;
import hook.Executable;

/**
 * Demande l'exécution d'un script
 * @author pf
 *
 */

public class ThrowScriptRequest implements Executable
{
	private ScriptHookNames n;
	
	public ThrowScriptRequest(ScriptHookNames n)
	{
		this.n = n;
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
		ArrayList<Byte> out = new ArrayList<Byte>();
		out.add((byte)(SerialProtocol.CALLBACK_SCRIPT.code+n.nbCapteur));
		return out;
	}


}
