package hook.types;

import obstacles.ObstacleRectangular;
import permissions.ReadOnly;
import permissions.ReadWrite;
import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import exceptions.WallCollisionDetectedException;
import strategie.GameState;
import utils.Config;
import utils.ConfigInfo;
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
	private ObstacleRectangular<ReadWrite> obstacle;
	
	public HookCollisionObstaclesFixes(Config config, Log log, GameState<?,ReadOnly> state)
	{
		super(config, log, state);
		// Le coefficient 2 vient du fait qu'on aura en fait un rectangle centré sur le robot,
		// et qu'on regardera aussi les obstacles derrière le robot
		int previsionCollision = config.getInt(ConfigInfo.PREVISION_COLLISION)*2;
		try {
			obstacle = new ObstacleRectangular<ReadWrite>(GameState.getPosition(state).clone(), GameState.getPosition(state).plusNewVector(new Vec2<ReadWrite>((int)(Math.cos(GameState.getOrientation(state))*previsionCollision), (int)(Math.sin(GameState.getOrientation(state))*previsionCollision))));
		} catch (FinMatchException e) {
			// Normalement impossible
			e.printStackTrace();
		}
	}

	@Override
	public void evaluate() throws FinMatchException, ScriptHookException, WallCollisionDetectedException
	{
		ObstacleRectangular.update(obstacle, GameState.getPosition(state), GameState.getOrientation(state));
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
