package obstacles;

import astar.arc.PathfindingNodes;
import astar.arc.SegmentTrajectoireCourbe;
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
	private SegmentTrajectoireCourbe segment;
	
	/**
	 * 
	 * @param objectifFinal
	 * @param intersection
	 * @param directionAvant de norme 1000
	 * @param vitesse
	 */
	public ObstacleTrajectoireCourbe(PathfindingNodes objectifFinal, PathfindingNodes intersection, Vec2 directionAvant, Speed vitesse)
	{
		// La position de cet obstacle est assez arbitraire...
		super(intersection.getCoordonnees());
		
		Vec2 directionApres = new Vec2(intersection.getOrientationFinale(objectifFinal));

		int rayonCourbure = vitesse.rayonCourbure();

		double angleDepart = directionAvant.getArgument();
		double angleRotation = (directionApres.getArgument() - angleDepart) % (2*Math.PI);

		if(angleRotation > Math.PI)
			angleRotation -= 2*Math.PI;

		int distanceAnticipation = (int) (rayonCourbure / Math.abs(Math.tan((Math.PI-Math.abs(angleRotation))/2)));

		Vec2 pointDepart = position.minusNewVector(directionAvant.scalarNewVector(distanceAnticipation/1000.));
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

		segment = new SegmentTrajectoireCourbe(objectifFinal, (int)(distanceAnticipation - rayonCourbure * Math.abs(angleRotation)), distanceAnticipation, pointDepart.clone(), directionAvant.clone());
	}

	/**
	 * Renvoie le segment SANS le PathfindingNodes
	 * @return
	 */
	public SegmentTrajectoireCourbe getSegment()
	{
		return segment;
	}

}
