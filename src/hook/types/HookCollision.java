package hook.types;

import obstacles.ObstacleCircular;
import obstacles.ObstacleRectangular;
import strategie.GameState;
import utils.Config;
import utils.Log;
import utils.Vec2;

/**
 * Hook pour gérer la collision avec les éléments de jeux
 * @author pf
 *
 */

public class HookCollision extends HookPosition
{
	
	ObstacleCircular obstacle;

	public HookCollision(Config config, Log log, GameState<?> state, ObstacleCircular o)
	{
		super(config, log, state, o.getPosition(), o.getRadius());
		this.obstacle = o;
	}

	@Override
	public boolean simulated_evaluate(Vec2 pointA, Vec2 pointB, long date)
	{
		ObstacleRectangular r = new ObstacleRectangular(pointA, pointB);
		return r.isColliding(obstacle);
	}
	
}
