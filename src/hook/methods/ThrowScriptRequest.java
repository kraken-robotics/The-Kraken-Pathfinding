package hook.methods;

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
	public String toSerial()
	{
		return "scr "+n.ordinal();
	}


}
