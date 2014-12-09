package hook.types;

import exceptions.FinMatchException;
import smartMath.Vec2;
import strategie.GameState;
import utils.Config;
import utils.Log;
import hook.Hook;

public class HookDate extends Hook {

	private long date;
	
	public HookDate(Config config, Log log, GameState<?> state, long date)
	{
		super(config, log, state);
		this.date = date;
	}

	@Override
	public boolean evaluate() throws FinMatchException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean simulated_evaluate(Vec2 pointA, Vec2 pointB, long date) {
		// vrai si on a dépassé la date donné au constructeur
		return date > this.date;
	}

}
