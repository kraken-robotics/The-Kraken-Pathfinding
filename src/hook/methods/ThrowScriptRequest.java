package hook.methods;

import java.util.ArrayList;

import pathfinding.GameState;
import permissions.ReadWrite;
import robot.RobotChrono;
import scripts.ScriptHookNames;
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
	public ArrayList<String> toSerial()
	{
		ArrayList<String> out = new ArrayList<String>();
		out.add("scr");
		out.add(String.valueOf(n.ordinal()));
		return out;
	}


}
