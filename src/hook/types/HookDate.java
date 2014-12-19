package hook.types;

import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import smartMath.Vec2;
import strategie.GameState;
import utils.Config;
import utils.Log;
import hook.Hook;

public class HookDate extends Hook {

	private long date_hook;
	
	public HookDate(Config config, Log log, GameState<?> state, long date)
	{
		super(config, log, state);
		this.date_hook = date;
	}

	@Override
	public void evaluate() throws FinMatchException, ScriptHookException {
		if(System.currentTimeMillis() - Config.getDateDebutMatch() > date_hook)
			trigger();
	}

	@Override
	public boolean simulated_evaluate(Vec2 pointA, Vec2 pointB, long date_appel) {
//		log.debug("Hook date: appel="+date_appel+", date_hook="+this.date_hook, this);
		return date_appel > this.date_hook;
	}

}
