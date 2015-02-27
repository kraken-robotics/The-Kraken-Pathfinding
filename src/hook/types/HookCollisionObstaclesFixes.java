package hook.types;

import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import strategie.GameState;
import utils.Config;
import utils.Log;
import utils.Vec2;
import hook.Hook;

/**
 * Hook qui s'active si le robot va percuter un mur très prochainement (vérification en translation uniquement)
 * @author pf
 *
 */

public class HookCollisionObstaclesFixes extends Hook
{

	public HookCollisionObstaclesFixes(Config config, Log log, GameState<?> state)
	{
		super(config, log, state);
	}

	@Override
	public void evaluate() throws FinMatchException, ScriptHookException
	{
		// TODO prévision collision
	}

	// Il n'y a pas de simulation pour ce hook
	@Override
	public boolean simulated_evaluate(Vec2 pointA, Vec2 pointB, long date)
	{
		return false;
	}

}
