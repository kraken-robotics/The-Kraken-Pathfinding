package obstacles;

import permissions.Permission;
import permissions.ReadWrite;
import utils.Vec2;

public abstract class ObstacleAvecAngle<T extends Permission> extends Obstacle<T>
{

	protected double angle, cos, sin;

	public ObstacleAvecAngle(Vec2<T> position, double angle) {
		super(position);
		this.angle = angle;
		cos = Math.cos(angle);
		sin = Math.sin(angle);

	}
	
	protected static void setAngle(ObstacleAvecAngle<ReadWrite> o, double angle)
	{
		o.angle = angle;
		o.cos = Math.cos(angle);
		o.sin = Math.sin(angle);
	}

}
