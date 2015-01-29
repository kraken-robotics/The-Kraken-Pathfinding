package obstacles;

import utils.ConfigInfo;
import utils.Vec2;

/**
 * Obstacle formé par le robot lorsqu'il effectue une trajectoire courbe
 * @author pf
 *
 */

public class ObstacleCouronnePartielle extends ObstacleRectanglesCollection
{
	/**
	 * Appel simplifié
	 * @param positionDebutRotation
	 * @param directionRobot
	 * @param angleRotation
	 * @param distanceAnticipation
	 */
	public ObstacleCouronnePartielle(Vec2 positionDebutRotation, Vec2 directionRobot, double angleRotation, double distanceAnticipation)
	{
		this(directionRobot.rotateNewVector(-Math.PI/2).scalarNewVector((float) (distanceAnticipation*Math.tan(Math.PI/2-angleRotation/2))), angleRotation, positionDebutRotation, Math.atan2(directionRobot.y, directionRobot.x));
	}
	
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
	public ObstacleCouronnePartielle(Vec2 centreCercle, double angleRotation, Vec2 pointDepart, double angleDepart)
	{
		super(centreCercle);
		int largeurRobot = config.getInt(ConfigInfo.LARGEUR_ROBOT);
		int longueurRobot = config.getInt(ConfigInfo.LONGUEUR_ROBOT);
		float R = centreCercle.distance(pointDepart);
		nb_rectangles = (int)(2*Math.abs(angleRotation)*R/longueurRobot)+1;
		ombresRobot = new ObstacleRectangular[nb_rectangles];
		for(int i = 0; i < nb_rectangles-1; i++)
			ombresRobot[i] = new ObstacleRectangular(pointDepart.rotateNewVector(i*angleRotation/(nb_rectangles-1), centreCercle), longueurRobot, largeurRobot, angleDepart+i*angleRotation/(nb_rectangles-1));
		ombresRobot[nb_rectangles-1] = new ObstacleRectangular(pointDepart.rotateNewVector(angleRotation, centreCercle), longueurRobot, largeurRobot, angleDepart+angleRotation);
	}

}
