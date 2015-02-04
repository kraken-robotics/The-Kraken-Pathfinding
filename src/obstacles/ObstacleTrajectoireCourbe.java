package obstacles;

import robot.Speed;
import utils.ConfigInfo;
import utils.Vec2;

/**
 * Obstacle formé par le robot lorsqu'il effectue une trajectoire courbe
 * @author pf
 *
 */

public class ObstacleTrajectoireCourbe extends ObstacleRectanglesCollection
{
	private double angleRotation;
	private int distanceAnticipation;
	private int rayonCourbure;
	
	/**
	 * 
	 * @param intersection
	 * @param directionAvant Vec2 pointant dans une direction, de norme 1000
	 * @param directionApres Vec2 pointant dans une direction, de norme 1000
	 * @param vitesse
	 */
	public ObstacleTrajectoireCourbe(Vec2 intersection, Vec2 directionAvant, Vec2 directionApres, Speed vitesse)
	{
		// La position de cet obstacle est assez arbitraire...
		super(intersection);
		rayonCourbure = vitesse.rayonCourbure();

		double angleDepart = directionAvant.getArgument();
		angleRotation = (directionApres.getArgument() - angleDepart) % (2*Math.PI);

		if(angleRotation > Math.PI)
			angleRotation -= 2*Math.PI;

		distanceAnticipation = (int) (rayonCourbure / Math.abs(Math.tan((Math.PI-angleRotation)/2)));

		Vec2 pointDepart = intersection.minusNewVector(directionAvant.scalarNewVector(distanceAnticipation/1000.));
		Vec2 orthogonalDirectionAvant = directionAvant.rotateNewVector(Math.PI/2);

		// Afin de placer le centre du cercle entre les deux directions
		if(orthogonalDirectionAvant.dot(directionApres) < 0)
			orthogonalDirectionAvant.scalar(-1);

		Vec2 centreCercle = pointDepart.plusNewVector(orthogonalDirectionAvant.scalarNewVector(rayonCourbure/1000.));

		int largeurRobot = config.getInt(ConfigInfo.LARGEUR_ROBOT);
		int longueurRobot = config.getInt(ConfigInfo.LONGUEUR_ROBOT);
//		double angleEntreOmbre = Math.atan2(longueurRobot/2, rayonCourbure+largeurRobot/2);
//		nb_rectangles = (int)(Math.abs(angleRotation/angleEntreOmbre))+1;
		nb_rectangles = 10;
		ombresRobot = new ObstacleRectangular[nb_rectangles];
		for(int i = 0; i < nb_rectangles-1; i++)
			ombresRobot[i] = new ObstacleRectangular(pointDepart.rotateNewVector(i*angleRotation/(nb_rectangles-1), centreCercle), longueurRobot, largeurRobot, angleDepart+i*angleRotation/(nb_rectangles-1));
		ombresRobot[nb_rectangles-1] = new ObstacleRectangular(pointDepart.rotateNewVector(angleRotation, centreCercle), longueurRobot, largeurRobot, angleDepart+angleRotation);
	}

	/**
	 * Donne la différence de distance entre la trajectoire en ligne brisée et la trajectoire courbe
	 * Cette distance est positive car la trajectoire courbe réduit la distance parcourue
	 * @return
	 */
	public int getDifferenceDistance()
	{
		return (int)(2 * distanceAnticipation - rayonCourbure * angleRotation);
	}
	
}
