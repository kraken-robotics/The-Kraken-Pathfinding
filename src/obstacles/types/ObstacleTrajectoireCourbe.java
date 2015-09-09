package obstacles.types;

import pathfinding.thetastar.LocomotionArc;
import pathfinding.thetastar.RayonCourbure;
import permissions.ReadOnly;
import permissions.ReadWrite;
import utils.Vec2;

/**
 * Obstacle formé par le robot lorsqu'il effectue une trajectoire courbe
 * @author pf
 *
 */

public class ObstacleTrajectoireCourbe extends ObstacleRectanglesCollection
{	
	/**
	 * Constructeur d'un obstacle à partir d'un arc
	 * @param arc
	 */
	public ObstacleTrajectoireCourbe(LocomotionArc arc)
	{
		// La position de cet obstacle est inutile...
		super(null);
		
		double angleDepart = arc.getAngleDepart();
		Vec2<ReadOnly> centreCercleRotation = arc.getCentreCercleRotation();
		Vec2<ReadOnly> destination = arc.getDestination();
		Vec2<ReadWrite> pointDepart = arc.getPointDepart().clone();
		Vec2<ReadWrite> normale = arc.getNormaleAuDemiPlan().clone();
		RayonCourbure rayon = arc.getRayonCourbure();
		
		nbRectangles = 0;
		double nouveauCos, ancienCos;
		double pasAngle = rayon.angleEntreDeuxOmbres, angleRobotActuel = angleDepart;
		double cosAngle = Math.cos(pasAngle);
		double sinAngle = Math.sin(pasAngle);
		ombresRobot = new ObstacleRectangular[rayon.nbOmbresMax+1];
		ombresRobot[nbRectangles] = new ObstacleRectangular(pointDepart.getReadOnly(), longueurRobot, largeurRobot, angleDepart);
		nouveauCos = normale.dot(destination.minusNewVector(pointDepart)) / 1000.;
		nouveauCos *= nouveauCos;
		nouveauCos /= destination.squaredDistance(pointDepart);
		do {
			ancienCos = nouveauCos;
			angleRobotActuel += pasAngle;
			nbRectangles++;
			if(nbRectangles == rayon.nbOmbresMax)
				break;
			Vec2.rotate(normale, cosAngle, sinAngle, centreCercleRotation);
			ombresRobot[nbRectangles] = new ObstacleRectangular(Vec2.rotate(pointDepart, cosAngle, sinAngle, centreCercleRotation).getReadOnly(), longueurRobot, largeurRobot, angleRobotActuel);
			
			nouveauCos = normale.dot(destination.minusNewVector(pointDepart)) / 1000.;
			nouveauCos *= nouveauCos;
			nouveauCos /= destination.squaredDistance(pointDepart);
		} while(nouveauCos > ancienCos);
		ombresRobot[rayon.nbOmbresMax] = new ObstacleRectangular(pointDepart.getReadOnly(), destination);
	}
}
