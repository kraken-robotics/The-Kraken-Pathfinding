package hook.types;

import permissions.ReadOnly;
import permissions.ReadWrite;
import strategie.GameState;
import utils.Config;
import utils.Log;
import utils.Vec2;
import hook.Hook;

public class HookDemiPlan extends Hook
{

	private final Vec2<ReadWrite> point, direction;
//	private boolean disabled = false;
	
	/**
	 * Constructeur sans paramètre, qui crée un hook qui n'est jamais appelable.
	 * Les paramètres sont donnés dans update.
	 * @param config
	 * @param log
	 * @param state
	 */
	public HookDemiPlan(Log log, GameState<?, ReadOnly> state)
	{
		super(log, state);
		point = new Vec2<ReadWrite>();
		direction = new Vec2<ReadWrite>();
	}
	
	/**
	 * Constructeur
	 * @param config
	 * @param log
	 * @param state
	 */
	public HookDemiPlan(Log log, GameState<?,ReadOnly> state, Vec2<ReadOnly> point, Vec2<ReadOnly> direction)
	{
		super(log, state);
		this.point = point.clone();
		this.direction = direction.clone();
	}

	
	/**
	 * Désactive le hook
	 */
/*	public void setDisabled()
	{
		disabled = true;
	}*/
	
/*	@Override
	public void evaluate() throws FinMatchException, ScriptHookException,
			WallCollisionDetectedException, ChangeDirectionException
	{
		Vec2<ReadOnly> positionRobot = GameState.getPosition(state);
//		log.debug("Evaluation en "+positionRobot+", point: "+point, this);
		if(!disabled && positionRobot.minusNewVector(point).dot(direction) > 0)
		{
//			log.debug("Exécution du hook!", this);
			trigger();
			disabled = true;
		}
	}*/
	
	/**
	 * Mise à jour des paramètres du hooks
	 */
	public void update(Vec2<ReadOnly> direction, Vec2<ReadOnly> point)
	{
//		disabled = false;
		Vec2.copy(direction, this.direction);
		Vec2.copy(point, this.point);
	}

	/**
	 * Pour vérifier si le hook peut être déclenché sur un segment [A,B], il
	 * faut juste vérifier s'il peut l'être en A ou en B.
	 */
	@Override
	public boolean simulated_evaluate(Vec2<ReadOnly> pointA, Vec2<ReadOnly> pointB, long date)
	{
		return pointA.minusNewVector(point).dot(direction) > 0 ||
				pointB.minusNewVector(point).dot(direction) > 0;
	}

}
