package exceptions;

import scripts.ScriptHookNames;
import table.GameElementNames;

/**
 * Exception levée par un hook
 * @author pf
 *
 */

public class ScriptHookException extends Exception
{
	private static final long serialVersionUID = -960091158805232282L;
	private ScriptHookNames script;
	private GameElementNames id;

	public ScriptHookException(ScriptHookNames script, GameElementNames id)
	{
		super();
		this.script = script;
		this.id = id;
	}
	
	public ScriptHookNames getNomScript()
	{
		return script;
	}
	
	public GameElementNames getVersion()
	{
		return id;
	}

}
