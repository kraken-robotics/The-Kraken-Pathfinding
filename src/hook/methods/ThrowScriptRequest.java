package hook.methods;

import java.util.ArrayList;

import permissions.ReadWrite;
import robot.RobotChrono;
import scripts.ScriptHookNames;
import strategie.GameState;
import hook.Executable;

/**
 * Demande l'exécution d'un script
 * @author pf
 *
 */

public class ThrowScriptRequest implements Executable
{
	private ScriptHookNames n;
	private int param;
	
	public ThrowScriptRequest(ScriptHookNames n, int param)
	{
		this.n = n;
		this.param = param;
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
		out.add(String.valueOf(param));
		return out;
	}


}
