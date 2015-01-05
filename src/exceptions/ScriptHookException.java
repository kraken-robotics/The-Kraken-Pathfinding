package exceptions;

import scripts.ScriptHookNames;

/**
 * Exception levée par un hook
 * @author pf
 *
 */

public class ScriptHookException extends Exception
{
	private static final long serialVersionUID = -960091158805232282L;
	private ScriptHookNames script;

	public ScriptHookException(ScriptHookNames script)
	{
		super();
		this.script = script;
	}
	
	public ScriptHookNames getNomScript()
	{
		return script;
	}

}
