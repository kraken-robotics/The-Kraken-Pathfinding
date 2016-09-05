package table;

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
	TRUC(new Vec2<ReadOnly>(1410, 150), -1),
	MACHIN(new Vec2<ReadOnly>(1410, 150), -1);

	private ObstacleCircular obstacle;

	public static final GameElementNames[] values;
	
	static
	{
		values = values();
	}
	
	public ObstacleCircular getObstacle()
	{
		return obstacle;
	}

	private GameElementNames(Vec2<ReadOnly> position, int radius)
	{
		obstacle = new ObstacleCircular(position, radius);
	}
	
}
