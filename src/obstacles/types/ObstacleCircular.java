package obstacles.types;

import utils.Vec2RO;

/**
 * Obstacle circulaire
 * @author pf
 *
 */
public class ObstacleCircular extends Obstacle
{
	// le Vec2 "position" indique le centre de l'obstacle
	
	// rayon de cet obstacle
	public final int radius;
	public final int squared_radius;
	
	public ObstacleCircular(Vec2RO position, int rad)
	{
		super(position);
		this.radius = rad;
		squared_radius = rad * rad;
	}

	@Override
	public String toString()
	{
		return super.toString()+", rayon: "+radius;
	}

	@Override
	public double squaredDistance(Vec2RO position)
	{
		double out = Math.max(0, position.distance(this.position) - radius);
		return out * out;
	}

	@Override
	public boolean isColliding(ObstacleRectangular o)
	{
		return o.squaredDistance(o.position.getReadOnly()) < radius*radius;
	}
}
