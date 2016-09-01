package obstacles.types;

import pathfinding.astarCourbe.arcs.ArcCourbe;
import utils.Vec2;
import utils.permissions.ReadOnly;

/**
 * Obstacle d'un arc de trajectoire courbe
 * Construit à partir de plein d'obstacles rectangulaires
 * @author pf
 *
 */

public class ObstacleArcCourbe extends Obstacle
{
	public ObstacleArcCourbe(ArcCourbe arc)
	{
		super(null);
		nbRectangles = arc.getNbPoints();
		ombresRobot = new ObstacleRectangular[nbRectangles];
		for(int i = 0; i < nbRectangles; i++)
			ombresRobot[i] = new ObstacleRectangular(arc.getPoint(i), false);
	}

	protected ObstacleRectangular[] ombresRobot;
	protected int nbRectangles;

	@Override
	public double squaredDistance(Vec2<ReadOnly> position)
	{
		double min = Double.MAX_VALUE;
		for(int i = 0; i < nbRectangles; i++)
		{
			min = Math.min(min, ombresRobot[i].squaredDistance(position));
			if(min == 0)
				return 0;
		}
		return min;
	}

	@Override
	public boolean isColliding(ObstacleRectangular obs) {
		for(int i = 0; i < nbRectangles; i++)
			if(obs.isColliding(ombresRobot[i]))
				return true;
		return false;
	}

}
