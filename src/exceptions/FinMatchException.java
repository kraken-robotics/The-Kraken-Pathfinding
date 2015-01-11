package exceptions;

import scripts.ScriptHookNames;

/**
 * Exception levée lorsque le match est terminé.
 * @author pf
 *
 */

public class FinMatchException extends ScriptHookException
{

	private static final long serialVersionUID = -960091158805232282L;

	public FinMatchException()
	{
		super(ScriptHookNames.FUNNY_ACTION);
	}
	
}
