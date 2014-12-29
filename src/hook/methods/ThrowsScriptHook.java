package hook.methods;

import robot.RobotChrono;
import strategie.GameState;
import hook.Executable;
import enums.ScriptHookNames;
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
