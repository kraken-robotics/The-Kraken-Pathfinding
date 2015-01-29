package obstacles;

import utils.Vec2;

/**
 * Obstacle formé par le robot lorsqu'il effectue une trajectoire courbe
 * @author pf
 *
 */

public class ObstacleCouronnePartielle extends ObstacleRectanglesCollection
{
	
	/**
	 * angleRotation dit de combien on tourne; c'est donc un angle relatif. Il suit
	 * la convention trigonométrique (angle positif: on tourne à gauche)
	 * @param centreCercle
	 * @param angleRotation
	 * @param pointDepart
	 * @param largeurRobot
	 * @param longueurRobot
	 * @param angleDepart
	 */
	public ObstacleCouronnePartielle(Vec2 centreCercle, double angleRotation, Vec2 pointDepart, int largeurRobot, int longueurRobot, double angleDepart)
	{
		super(centreCercle);
		float R = centreCercle.distance(pointDepart);
		nb_rectangles = (int)(2*Math.abs(angleRotation)*R/longueurRobot)+1;
		ombresRobot = new ObstacleRectangular[nb_rectangles];
		for(int i = 0; i < nb_rectangles-1; i++)
			ombresRobot[i] = new ObstacleRectangular(pointDepart.rotateNewVector(i*angleRotation/(nb_rectangles-1), centreCercle), longueurRobot, largeurRobot, angleDepart+i*angleRotation/(nb_rectangles-1));
		ombresRobot[nb_rectangles-1] = new ObstacleRectangular(pointDepart.rotateNewVector(angleRotation, centreCercle), longueurRobot, largeurRobot, angleDepart+angleRotation);
	}

}
