package hook.types;

import exceptions.ChangeDirectionException;
import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import exceptions.WallCollisionDetectedException;
import strategie.GameState;
import utils.Config;
import utils.Log;
import vec2.ReadOnly;
import vec2.Vec2;
import hook.Hook;

/**
 * Hook déclenché sur une date
 * @author pf
 *
 */

public class HookDate extends Hook {

	protected long date_hook;
	
	public HookDate(Config config, Log log, GameState<?> state, long date)
	{
		super(config, log, state);
		this.date_hook = date;
	}

	@Override
	public void evaluate() throws FinMatchException, ScriptHookException, WallCollisionDetectedException, ChangeDirectionException {
		if(System.currentTimeMillis() - Config.getDateDebutMatch() > date_hook)
			trigger();
	}

	@Override
	public boolean simulated_evaluate(Vec2<? extends ReadOnly> pointA, Vec2<? extends ReadOnly> pointB, long date_appel) {
//		log.debug("Hook date: appel="+date_appel+", date_hook="+this.date_hook, this);
		return date_appel > this.date_hook;
	}

}
