package table;

import obstacles.types.Obstacle;
import obstacles.types.ObstacleCircular;
import utils.Vec2;
import utils.permissions.ReadOnly;

/**
 * Enumérations contenant tous les éléments de jeux
 * @author pf
 *
 */

// DEPENDS_ON_RULES

public enum GameElementNames {
	TRUC(new ObstacleCircular(new Vec2<ReadOnly>(1410, 150), -1)),
	MACHIN(new ObstacleCircular(new Vec2<ReadOnly>(1410, 150), -1));

	public final Obstacle obstacle;

	private GameElementNames(Obstacle obs)
	{
		obstacle = obs;
	}
	
}
