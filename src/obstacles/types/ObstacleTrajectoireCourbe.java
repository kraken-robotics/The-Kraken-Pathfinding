package obstacles.types;

import java.math.BigDecimal;
import java.math.RoundingMode;

import obstacles.ClothoidesComputer;
import pathfinding.VitesseCourbure;
import pathfinding.astarCourbe.AStarCourbeNode;
import permissions.ReadOnly;
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
	public ObstacleTrajectoireCourbe(AStarCourbeNode node, ClothoidesComputer courbe)
	{
		super(null);
	/*		for(int s = -nbPoints/2; s < nbPoints/2; s++)
		{
			calculeXY(new BigDecimal(2.*s*sMax/nbPoints).setScale(15, RoundingMode.HALF_EVEN), VitesseCourbure.DROITE_LENTEMENT);
			new ObstacleRectangular(new Vec2<ReadOnly>((int)(250*x.doubleValue()), (int)(1000+250*y.doubleValue())), 10, 10, 0);
		}
		for(int s = -nbPoints/2; s < nbPoints/2; s++)
		{
			calculeXY(new BigDecimal(2.*s*sMax/nbPoints).setScale(15, RoundingMode.HALF_EVEN), VitesseCourbure.DROITE_LENTEMENT);
			new ObstacleRectangular(new Vec2<ReadOnly>((int)(500*x.doubleValue()), (int)(1000+500*y.doubleValue())), 10, 10, 0);
		}
		for(int s = -nbPoints/2; s < nbPoints/2; s++)
		{
			calculeXY(new BigDecimal(2.*s*sMax/nbPoints).setScale(15, RoundingMode.HALF_EVEN), VitesseCourbure.DROITE_LENTEMENT);
			new ObstacleRectangular(new Vec2<ReadOnly>((int)(500*2*x.doubleValue()), (int)(1000+500*2*y.doubleValue())), 10, 10, 0);
		}*/
		// TODO refaire complètement avec une clothoïde (développement limité sur https://fr.wikipedia.org/wiki/Clotho%C3%AFde)

		// La position de cet obstacle est inutile...
		/*
		double angleDepart = arc.getAngleDepart();
		Vec2<ReadOnly> centreCercleRotation = arc.getCentreCercleRotation();
		Vec2<ReadOnly> destination = arc.getDestination();
		Vec2<ReadWrite> pointDepart = arc.getPointDepart().clone();
		Vec2<ReadWrite> normale = arc.getNormaleAuDemiPlan().clone();
		RayonCourbure rayon = arc.getRayonCourbure();
//		log.debug("centreCercleRotation : "+centreCercleRotation);
//		log.debug("pointDepart : "+pointDepart);
		nbRectangles = 0;
		double nouveauCos, ancienCos;
		double pasAngle = rayon.angleEntreDeuxOmbres, angleRobotActuel = angleDepart;
		if(!arc.getSensTrigo())
			pasAngle = -pasAngle;
		double cosAngle = Math.cos(pasAngle);
		double sinAngle = Math.sin(pasAngle);
		ombresRobot = new ObstacleRectangular[rayon.nbOmbresMax+1];
//		log.debug("angleEntreDeuxOmbres = "+rayon.angleEntreDeuxOmbres);
//		log.debug("nbOmbresMax = "+rayon.nbOmbresMax);
		ombresRobot[nbRectangles] = new ObstacleRectangular(pointDepart.clone().getReadOnly(), longueurRobot, largeurRobot+2*marge, angleDepart);
//		ombresRobot[nbRectangles] = new ObstacleRectangular(pointDepart.clone().getReadOnly(), 100, 100, angleDepart);
		nouveauCos = normale.dot(destination.minusNewVector(pointDepart)) / 1000.;
		nouveauCos *= nouveauCos;
		
		nouveauCos /= destination.squaredDistance(pointDepart);
		while(true)
		{
			ancienCos = nouveauCos;
			Vec2.rotate(normale, cosAngle, sinAngle);

			nouveauCos = normale.dot(destination.minusNewVector(pointDepart));
			nouveauCos *= nouveauCos;
			nouveauCos /= destination.squaredDistance(pointDepart);
			nouveauCos /= normale.squaredLength();
			if(nouveauCos < ancienCos)
				break;

			angleRobotActuel += pasAngle;
//			ombresRobot[nbRectangles] = new ObstacleRectangular(Vec2.rotate(pointDepart, cosAngle, sinAngle, centreCercleRotation).clone().getReadOnly(), 100, 100, angleRobotActuel);
			ombresRobot[nbRectangles] = new ObstacleRectangular(Vec2.rotate(pointDepart, cosAngle, sinAngle, centreCercleRotation).clone().getReadOnly(), longueurRobot, largeurRobot+2*marge, angleRobotActuel);
			nbRectangles++;
			if(nbRectangles == rayon.nbOmbresMax)
			{
//				log.critical("Nb max atteint");
				break;
			}
			
//			log.debug(ancienCos+" "+nouveauCos);
		}
		ombresRobot[nbRectangles] = new ObstacleRectangular(pointDepart.clone().getReadOnly(), 10, 10, 0);		
		ombresRobot[nbRectangles] = new ObstacleRectangular(destination.clone().getReadOnly(), 10, 10, 0);
		ombresRobot[nbRectangles] = new ObstacleRectangular(pointDepart.getReadOnly(), destination);
		nbRectangles++;*/
	}
}
