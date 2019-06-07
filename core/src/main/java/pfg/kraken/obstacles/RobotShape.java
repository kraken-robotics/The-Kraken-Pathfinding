package pfg.kraken.obstacles;

/**
 * The shape of the robot
 * @author pf
 *
 */
public class RobotShape extends RectangularObstacle
{
	private static final long serialVersionUID = -812328549496641800L;

	public RobotShape(RectangularObstacle obs) {
		super(obs.position, obs.coinHautDroite, obs.coinBasGauche, obs.angle);
	}
	
	public RobotShape clone()
	{
		return new RobotShape(this);
	}
}
