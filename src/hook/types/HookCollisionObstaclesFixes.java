package hook.types;

import obstacles.ObstacleRectangular;
import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import exceptions.WallCollisionDetectedException;
import strategie.GameState;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import vec2.ReadOnly;
import vec2.ReadWrite;
import vec2.Vec2;
import hook.Hook;

/**
 * Hook qui s'active si le robot va percuter un mur très prochainement (vérification en translation uniquement)
 * @author pf
 *
 */

public class HookCollisionObstaclesFixes extends Hook
{
	private ObstacleRectangular obstacle;
	
	public HookCollisionObstaclesFixes(Config config, Log log, GameState<?> state)
	{
		super(config, log, state);
		// Le coefficient 2 vient du fait qu'on aura en fait un rectangle centré sur le robot,
		// et qu'on regardera aussi les obstacles derrière le robot
		int previsionCollision = config.getInt(ConfigInfo.PREVISION_COLLISION)*2;
		try {
			obstacle = new ObstacleRectangular(state.robot.getPosition(), state.robot.getPosition().plusNewVector(new Vec2<ReadWrite>((int)(Math.cos(state.robot.getOrientation())*previsionCollision), (int)(Math.sin(state.robot.getOrientation())*previsionCollision))).getReadOnly());
		} catch (FinMatchException e) {
			// Normalement impossible
			e.printStackTrace();
		}
	}

	@Override
	public void evaluate() throws FinMatchException, ScriptHookException, WallCollisionDetectedException
	{
		obstacle.update(state.robot.getPosition(), state.robot.getOrientation());
		if(obstacle.isCollidingObstacleFixe())
			throw new WallCollisionDetectedException();
	}

	// Il n'y a pas de simulation pour ce hook
	@Override
	public boolean simulated_evaluate(Vec2<ReadOnly> pointA, Vec2<ReadOnly> pointB, long date)
	{
		return false;
	}

}
