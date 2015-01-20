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
	private ObstacleRectangular[] ombresRobot;
	private int nb_rectangles;
	
	public ObstacleRotationRobot(Vec2 position, double angleDepart, double angleArrivee)
	{
		super(position);
		double angleRelatif = (angleArrivee-angleDepart) % (2*Math.PI);
		log.debug("Math.abs(angleRelatif)/anglePas = "+Math.abs(angleRelatif)/anglePas, this);
		nb_rectangles = (int)Math.ceil(Math.abs(angleRelatif)/anglePas)+1;
		ombresRobot = new ObstacleRectangular[nb_rectangles]; // le dernier est à part
		for(int i = 0; i < nb_rectangles-1; i++)
			ombresRobot[i] = new ObstacleRectangular(position, longueurRobot, largeurRobot, angleDepart-i*anglePas*Math.signum(angleRelatif));

		ombresRobot[nb_rectangles-1] = new ObstacleRectangular(position, longueurRobot, largeurRobot, angleArrivee);
	}

	@Override
	public boolean isProcheObstacle(Vec2 point, int distance)
	{
		for(int i = 0; i < nb_rectangles; i++)
			if(ombresRobot[i].isProcheObstacle(point, distance))
				return true;
		return false;
	}

	@Override
	public boolean isInObstacle(Vec2 point)
	{
		for(int i = 0; i < nb_rectangles; i++)
			if(ombresRobot[i].isInObstacle(point))
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
			if(ombresRobot[i].isColliding(o))
				return true;
		return false;
	}
	
	/**
	 * Utilisé pour l'affichage
	 * @return
	 */
	public ObstacleRectangular[] getOmbresRobot()
	{
		return ombresRobot;
	}

	/**
	 * Y a-t-il collision avec un obstacle fixe?
	 * @return
	 */
	public boolean isCollidingObstacleFixe()
	{
		for(ObstaclesFixes o: ObstaclesFixes.values())
			if(isColliding(o.getObstacle()))
				return true;
		return false;
	}
	
}
