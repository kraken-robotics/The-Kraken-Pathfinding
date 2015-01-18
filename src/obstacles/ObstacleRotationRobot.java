package obstacles;

import utils.Vec2;

/**
 * Cet obstacle a une forme étrange, car c'est la forme du robot quand il tourne.
 * Cet obstacle sert à savoir si on peut tourner en étant près d'un mur.
 * On utilise une approximation de cette forme avec plusieurs rectangles
 * (comme si le robot "sautait" d'angle en angle).
 * @author pf
 *
 */

public class ObstacleRotationRobot extends Obstacle
{
	private ObstacleRectangular[] robot;
	private int nb_rectangles;
	
	public ObstacleRotationRobot(Vec2 position, double angleDepart, double angleRelatifRotation)
	{
		super(position);
		nb_rectangles = (int)Math.floor(Math.abs(angleRelatifRotation)/anglePas);
		robot = new ObstacleRectangular[nb_rectangles];
		for(int i = 0; i < nb_rectangles; i++)
			robot[i] = new ObstacleRectangular(position, longueurRobot, largeurRobot, angleDepart-i*anglePas*Math.signum(angleRelatifRotation));
	}

	@Override
	public boolean isProcheObstacle(Vec2 point, int distance)
	{
		for(int i = 0; i < nb_rectangles; i++)
			if(robot[i].isProcheObstacle(point, distance))
				return true;
		return false;
	}

	@Override
	public boolean isInObstacle(Vec2 point)
	{
		for(int i = 0; i < nb_rectangles; i++)
			if(robot[i].isInObstacle(point))
				return true;
		return false;
	}	
	
	/**
	 * Y a-t-il collision avec un obstacle fixe?
	 * Il y a collision s'il y a collision avec au moins un des robots tournés.
	 * @param o
	 * @return
	 */
	public boolean isColliding(ObstacleRectangular o)
	{
		for(int i = 0; i < nb_rectangles; i++)
			if(robot[i].isColliding(o))
				return true;
		return false;
	}
	
}
