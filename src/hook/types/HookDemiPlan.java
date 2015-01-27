package hook.types;

import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import exceptions.WallCollisionDetectedException;
import strategie.GameState;
import utils.Config;
import utils.Log;
import utils.Vec2;
import hook.Hook;

public class HookDemiPlan extends Hook
{

	private Vec2 point, direction;
	
	/**
	 * point appartient à la ligne qui sépare le demi-plan
	 * direction est un vecteur normal à la ligne de séparation de deux demi-plans, et
	 * qui pointe en direction du plan qui active le hook
	 * @param config
	 * @param log
	 * @param state
	 * @param point
	 * @param direction
	 */
	public HookDemiPlan(Config config, Log log, GameState<?> state, Vec2 point, Vec2 direction)
	{		
		super(config, log, state);
		this.point = point;
		this.direction = direction;
	}

	@Override
	public void evaluate() throws FinMatchException, ScriptHookException,
			WallCollisionDetectedException
	{
		Vec2 positionRobot = state.robot.getPosition();
		if(positionRobot.minusNewVector(point).dot(direction) > 0)
			trigger();
	}

	/**
	 * Pour vérifier si le hook peut être déclenché sur un segment [A,B], il
	 * faut juste vérifier s'il peut l'être en A ou en B.
	 */
	@Override
	public boolean simulated_evaluate(Vec2 pointA, Vec2 pointB, long date) {
		return pointA.minusNewVector(point).dot(direction) > 0 ||
				pointB.minusNewVector(point).dot(direction) > 0;
	}

}
