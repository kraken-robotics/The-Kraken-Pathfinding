package buffer;

import scripts.ScriptHookNames;

/**
 * Une information sur un hook qui vient d'être exécuté par la STM
 * @author pf
 *
 */

public class IncomingHook {

	public ScriptHookNames script;
	public int param;

	public IncomingHook(ScriptHookNames script, int param)
	{
		this.script = script;
		this.param = param;
	}
	
}
