package obstacles.types;

import permissions.ReadOnly;
import utils.Vec2;

/**
 * Obstacle triangulaire. Sert à déterminer la "collision" entre le cône de détection d'un capteur et un obstacle.
 * Le cône du capteur est approché par un triangle (pour les infrarouges, le cône est si mince que c'est presque pas tricher).
 * @author pf
 *
 */

public class ObstacleTriangular extends Obstacle
{

	public ObstacleTriangular(Vec2<ReadOnly> position) {
		super(position);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isProcheObstacle(Vec2<ReadOnly> point, int distance) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInObstacle(Vec2<ReadOnly> point)
	{
		return false;
	}

}
