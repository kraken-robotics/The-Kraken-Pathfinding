package table;

import obstacles.ObstacleCircular;
import container.Service;
import permissions.ReadOnly;
import permissions.ReadWrite;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2;

/**
 * Cette classe contient les informations sur la situation
 * spatiale des capteurs sur le robot.
 * @author pf
 *
 */

@SuppressWarnings("unchecked")
public class Capteurs implements Service {
	// DEPENDS ON ROBOT
	
	protected Log log;
	
	private final int nbCapteurs = 2;
	
	private final int nbCouples = 1;
	
	/**
	 * Les ultrasons ont un cône de 35°
	 */
	private final double angleCone = 35.*Math.PI/180.;
	
	private final double cos = Math.cos(Math.PI/2-angleCone);
	
	/**
	 * Les positions relatives des capteurs par rapport au centre du
	 * robot lorsque celui-ci a une orientation nulle.
	 */
	public final Vec2<ReadOnly>[] positionsRelatives;
	
	public final Vec2<ReadOnly>[][] cones;
	
	/**
	 * coupleCapteurs[i] est un tableau qui contient les numéros des
	 * capteurs du couple i ainsi que la distance entre ces deux capteurs
	 */
	public static int[][] coupleCapteurs;

	/**
	 * L'orientation des capteurs lorsque le robot a une orientation nulle
	 */
	public static double[] orientationsRelatives;

	public Capteurs(Log log, Config config)
	{
		this.log = log;
		positionsRelatives = new Vec2[nbCapteurs];
		positionsRelatives[0] = new Vec2<ReadOnly>(100, 100);
		positionsRelatives[1] = new Vec2<ReadOnly>(100, -100);
/*
		positionsRelatives[2] = new Vec2<ReadOnly>(-100, 100);
		positionsRelatives[3] = new Vec2<ReadOnly>(100, 100);

		positionsRelatives[4] = new Vec2<ReadOnly>(-100, -100);
		positionsRelatives[5] = new Vec2<ReadOnly>(-100, 100);

		positionsRelatives[6] = new Vec2<ReadOnly>(100, -100);
		positionsRelatives[7] = new Vec2<ReadOnly>(-100, -100);
*/
		double angleDeBase;
		angleDeBase = angleCone/2;
//		angleDeBase = - Math.PI/4 + angleCone;
		
		orientationsRelatives = new double[nbCapteurs];
		orientationsRelatives[0] = -angleDeBase;
		orientationsRelatives[1] = angleDeBase;

/*		orientationsRelatives[2] = -angleDeBase + Math.PI/2;
		orientationsRelatives[3] = angleDeBase + Math.PI/2;

		orientationsRelatives[4] = -angleDeBase + Math.PI;
		orientationsRelatives[5] = angleDeBase + Math.PI;

		orientationsRelatives[6] = -angleDeBase + 3*Math.PI/2;
		orientationsRelatives[7] = angleDeBase + 3*Math.PI/2;
*/
		cones = new Vec2[nbCapteurs][3];
		for(int i = 0; i < nbCapteurs; i++)
		{
			cones[i][0] = new Vec2<ReadOnly>(orientationsRelatives[i]);
			cones[i][1] = new Vec2<ReadOnly>(orientationsRelatives[i]+Math.PI/2-angleCone);
			cones[i][2] = new Vec2<ReadOnly>(orientationsRelatives[i]-Math.PI/2+angleCone);
		}
		
		coupleCapteurs = new int[nbCouples][3];
		coupleCapteurs[0][0] = 0;
		coupleCapteurs[0][1] = 1;

/*		coupleCapteurs[1][0] = 2;
		coupleCapteurs[1][1] = 3;

		coupleCapteurs[2][0] = 4;
		coupleCapteurs[2][1] = 5;

		coupleCapteurs[3][0] = 6;
		coupleCapteurs[3][1] = 7;
	*/	
		for(int i = 0; i < nbCouples; i++)
			coupleCapteurs[i][2] = (int)positionsRelatives[coupleCapteurs[i][0]].distance(positionsRelatives[coupleCapteurs[i][1]]);
//		log.debug("distance: "+coupleCapteurs[0][2]);

		config.set(ConfigInfo.NB_CAPTEURS_PROXIMITE, nbCapteurs);
		config.set(ConfigInfo.NB_COUPLES_CAPTEURS_PROXIMITE, nbCouples);
	}
	
	/**
	 * Ce point peut-il être vu par ce capteur?
	 * Le point est dans le référentiel du robot
	 * @param point
	 * @param nbCapteur
	 * @return
	 */
	public boolean canBeSeen(Vec2<ReadOnly> point, int nbCapteur)
	{
		Vec2<ReadOnly> tmp = point.minusNewVector(positionsRelatives[nbCapteur]).getReadOnly();
		return tmp.dot(cones[nbCapteur][0]) > 0 && tmp.dot(cones[nbCapteur][1]) > 0 && tmp.dot(cones[nbCapteur][2]) > 0;
	}
	
	/**
	 * Retourne la position ajustée de l'obstacle vu.
	 * Cette position est donnée dans le référentiel du capteur qui voit
	 * @param nbCouple
	 * @param gauche: est-ce le capteur gauche qui voit l'obstacle?
	 * @param mesure
	 * @return
	 */
	public Vec2<ReadWrite> getPositionAjustee(int nbCouple, boolean gauche, int mesure)
	{
		int capteurQuiVoit = coupleCapteurs[nbCouple][gauche?0:1];
		int capteurQuiNeVoitPas = coupleCapteurs[nbCouple][gauche?1:0];
		Vec2<ReadWrite> intersectionPoint;
		int distance = coupleCapteurs[nbCouple][2];
		double b = -2.*distance*cos;
		double c = distance*distance - mesure*mesure;
		double delta = b*b-4*c;
		
/*		log.debug("gauche: "+gauche);
		log.debug("distance entre capteurs: "+distance);
		log.debug("mesure: "+mesure);
		log.debug("cos: "+cos);
	*/	
		if(delta < 0)
		{
//			log.debug("Pas d'intersection: delta négatif");
			return null;
		}

		// On prend la plus grande solution
		double s = (-b+Math.sqrt(delta))/2;
		if(s <= 0)
		{
//			log.debug("Pas d'intersection: distance négative");
			return null;
		}
			
/*		log.debug("Distance 1: "+s);
		log.debug("Distance 2: "+(-b-Math.sqrt(delta))/2);
*/
		if(gauche)
			intersectionPoint = new Vec2<ReadWrite>((int)s, angleCone);
		else
			intersectionPoint = new Vec2<ReadWrite>((int)s, -angleCone);

//		log.debug("Point avant repère: "+intersectionPoint);
		
		// changement de repère
		Vec2.plus(intersectionPoint, positionsRelatives[capteurQuiNeVoitPas]);

//		log.debug("Point après repère: "+intersectionPoint);
		
//		log.debug("Distance: "+intersectionPoint.distance(positionsRelatives[capteurQuiVoit]));
		
		if(!canBeSeen(intersectionPoint.getReadOnly(), capteurQuiVoit))
		{
//			log.debug("Ce point n'est pas visible");
			return null; // le point d'intersection n'est pas vu par le capteur qui voit l'obstacle
		}
		
//		log.debug("Autre point: "+new Vec2<ReadWrite>(mesure, gauche?angleCone:-angleCone));
		
		Vec2.plus(intersectionPoint, Vec2.plus(new Vec2<ReadWrite>(mesure, gauche?angleCone:-angleCone), positionsRelatives[capteurQuiVoit]));
		double longueur = intersectionPoint.length();
		Vec2.scalar(intersectionPoint, mesure/longueur);
		
		return intersectionPoint;
	}
	
	/**
	 * A quelle distance le capteur voit-il cet obstacle?
	 * @param nbCapteur
	 * @param o
	 * @return
	 */
	public int distanceSeen(int nbCapteur, ObstacleCircular o)
	{
		return 0;
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
}
