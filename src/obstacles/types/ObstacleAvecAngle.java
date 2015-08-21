package obstacles.types;

import permissions.ReadOnly;
import utils.Vec2;

/**
 * Classe abstraite d'obstacles qui possèdent une orientation
 * @author pf
 *
 * @param <T>
 */

public abstract class ObstacleAvecAngle extends Obstacle
{

	protected double angle, cos, sin;

	public ObstacleAvecAngle(Vec2<ReadOnly> position, double angle) {
		super(position);
		this.angle = angle;
		cos = Math.cos(angle);
		sin = Math.sin(angle);

	}
	
	protected static void setAngle(ObstacleAvecAngle o, double angle)
	{
		o.angle = angle;
		o.cos = Math.cos(angle);
		o.sin = Math.sin(angle);
	}

}
