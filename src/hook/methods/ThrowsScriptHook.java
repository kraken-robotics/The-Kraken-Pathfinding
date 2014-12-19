package hook.methods;

import hook.Executable;
import enums.ScriptHookNames;
import exceptions.ScriptHookException;

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
	
}
