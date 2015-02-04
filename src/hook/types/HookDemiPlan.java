package hook.types;

import exceptions.ChangeDirectionException;
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
	private boolean disabled = false;
	
	/**
	 * Constructeur sans paramètre, qui crée un hook qui n'est jamais appelable.
	 * Les paramètres sont donnés dans update.
	 * @param config
	 * @param log
	 * @param state
	 */
	public HookDemiPlan(Config config, Log log, GameState<?> state)
	{
		super(config, log, state);
	}
	
	/**
	 * Constructeur
	 * @param config
	 * @param log
	 * @param state
	 */
	public HookDemiPlan(Config config, Log log, GameState<?> state, Vec2 point, Vec2 direction)
	{
		super(config, log, state);
		this.point = point;
		this.direction = direction;
	}

	
	/**
	 * Désactive le hook
	 */
	public void setDisabled()
	{
		disabled = true;
	}
	
	@Override
	public void evaluate() throws FinMatchException, ScriptHookException,
			WallCollisionDetectedException, ChangeDirectionException
	{
		Vec2 positionRobot = state.robot.getPosition();
		if(!disabled && positionRobot.minusNewVector(point).dot(direction) > 0)
		{
			trigger();
			disabled = true;
		}
	}
	
	/**
	 * Mise à jour des paramètres du hooks
	 */
	public void update(Vec2 direction, Vec2 point)
	{
		disabled = false;
		this.direction = direction;
		this.point = point;
	}

	/**
	 * Pour vérifier si le hook peut être déclenché sur un segment [A,B], il
	 * faut juste vérifier s'il peut l'être en A ou en B.
	 */
	@Override
	public boolean simulated_evaluate(Vec2 pointA, Vec2 pointB, long date)
	{
		return pointA.minusNewVector(point).dot(direction) > 0 ||
				pointB.minusNewVector(point).dot(direction) > 0;
	}

}
