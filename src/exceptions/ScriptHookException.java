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
	private int id;

	public ScriptHookException(ScriptHookNames script, int id)
	{
		super();
		this.script = script;
		this.id = id;
	}
	
	public ScriptHookNames getNomScript()
	{
		return script;
	}
	
	public int getVersion()
	{
		return id;
	}

}
