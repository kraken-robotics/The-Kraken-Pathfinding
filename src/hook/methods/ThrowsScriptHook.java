package hook.methods;

import robot.RobotChrono;
import scripts.hooks.ScriptHookNames;
import strategie.GameState;
import hook.Executable;
import exceptions.ScriptHookException;

/**
 * Lève une exception prévenant la possibilité d'un script de hook
 * @author pf
 *
 */

public class ThrowsScriptHook implements Executable {

	private ScriptHookNames script;
	
	public ThrowsScriptHook(ScriptHookNames script)
	{
		this.script = script;
	}
	
	@Override
	public void execute() throws ScriptHookException
	{
		throw new ScriptHookException(script);
	}
	
	@Override
	public void updateGameState(GameState<RobotChrono> state)
	{}

}
