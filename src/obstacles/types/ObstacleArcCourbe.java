package obstacles.types;

import java.util.ArrayList;

import utils.Vec2RO;

/**
 * Obstacle d'un arc de trajectoire courbe
 * Construit à partir de plein d'obstacles rectangulaires
 * @author pf
 *
 */

public class ObstacleArcCourbe extends Obstacle
{
	public ObstacleArcCourbe()
	{
		super(new Vec2RO());
	}
	
	public ArrayList<ObstacleRectangular> ombresRobot = new ArrayList<ObstacleRectangular>();

	@Override
	public double squaredDistance(Vec2RO position)
	{
		double min = Double.MAX_VALUE;
		for(ObstacleRectangular o : ombresRobot)
		{
			min = Math.min(min, o.squaredDistance(position));
			if(min == 0)
				return 0;
		}
		return min;
	}

	@Override
	public boolean isColliding(ObstacleRectangular obs) {
		for(ObstacleRectangular o : ombresRobot)
			if(obs.isColliding(o))
				return true;
		return false;
	}

}
