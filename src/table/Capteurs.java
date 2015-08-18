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
	
	private static final int nbCapteurs = 12;
	
	private static final int nbCouples = 4;
	
	/**
	 * Les ultrasons ont un cône de 35°
	 * Les infrarouges, de 5°
	 */
	private final double[] angleCone = {35.*Math.PI/180., 5.*Math.PI/180.};
	
	private static final int ultrason = 0;
	private static final int infrarouge = 1;
	
	private final int nbUltrasons = 12;
	private int horizonCapteursSquared;
	
	/**
	 * Les positions relatives des capteurs par rapport au centre du
	 * robot lorsque celui-ci a une orientation nulle.
	 */
	public final Vec2<ReadOnly>[] positionsRelatives;
	
	/**
	 * Premier indice: numéro d'un capteur
	 * Deuxième indice: numéro d'une droite qui définit le cone
	 */
	public final Vec2<ReadOnly>[][] cones;

	private final double[] sin;
	
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
		orientationsRelatives = new double[nbCapteurs];

		/**
		 * Définition des infrarouges
		 */
		
		positionsRelatives[8] = new Vec2<ReadOnly>(100, 100);
		positionsRelatives[9] = new Vec2<ReadOnly>(-100, 100);
		positionsRelatives[10] = new Vec2<ReadOnly>(-100, -100);
		positionsRelatives[11] = new Vec2<ReadOnly>(100, -100);

		orientationsRelatives[8] = Math.PI/4;
		orientationsRelatives[9] = Math.PI/4 + Math.PI/2;
		orientationsRelatives[10] = Math.PI/4 + Math.PI;
		orientationsRelatives[11] = Math.PI/4 + 3*Math.PI/2;		
		
		/**
		 * Définition des ultrasons
		 */
				
		positionsRelatives[0] = new Vec2<ReadOnly>(100, 100);
		positionsRelatives[1] = new Vec2<ReadOnly>(100, -100);

		positionsRelatives[2] = new Vec2<ReadOnly>(-100, 100);
		positionsRelatives[3] = new Vec2<ReadOnly>(100, 100);

		positionsRelatives[4] = new Vec2<ReadOnly>(-100, -100);
		positionsRelatives[5] = new Vec2<ReadOnly>(-100, 100);

		positionsRelatives[6] = new Vec2<ReadOnly>(100, -100);
		positionsRelatives[7] = new Vec2<ReadOnly>(-100, -100);

		double angleDeBase;
//		angleDeBase = angleCone[0]/2;
		angleDeBase = - Math.PI/4 + 2*angleCone[0];
		
		orientationsRelatives[0] = -angleDeBase;
		orientationsRelatives[1] = angleDeBase;

		orientationsRelatives[2] = -angleDeBase + Math.PI/2;
		orientationsRelatives[3] = angleDeBase + Math.PI/2;

		orientationsRelatives[4] = -angleDeBase + Math.PI;
		orientationsRelatives[5] = angleDeBase + Math.PI;

		orientationsRelatives[6] = -angleDeBase + 3*Math.PI/2;
		orientationsRelatives[7] = angleDeBase + 3*Math.PI/2;

		cones = new Vec2[nbCapteurs][3];
		for(int i = 0; i < nbCapteurs; i++)
		{
			cones[i][0] = new Vec2<ReadOnly>(orientationsRelatives[i]);
			cones[i][1] = new Vec2<ReadOnly>(orientationsRelatives[i]+Math.PI/2-angleCone[i<nbUltrasons?ultrason:infrarouge]);
			cones[i][2] = new Vec2<ReadOnly>(orientationsRelatives[i]-Math.PI/2+angleCone[i<nbUltrasons?ultrason:infrarouge]);
		}
		
		sin = new double[2];
		sin[ultrason] = Math.sin(angleCone[ultrason]);
		sin[infrarouge] = Math.sin(angleCone[infrarouge]);		
		
		coupleCapteurs = new int[nbCouples][3];
		coupleCapteurs[0][0] = 0;
		coupleCapteurs[0][1] = 1;

		coupleCapteurs[1][0] = 2;
		coupleCapteurs[1][1] = 3;

		coupleCapteurs[2][0] = 4;
		coupleCapteurs[2][1] = 5;

		coupleCapteurs[3][0] = 6;
		coupleCapteurs[3][1] = 7;

		for(int i = 0; i < nbCouples; i++)
			coupleCapteurs[i][2] = (int)positionsRelatives[coupleCapteurs[i][0]].distance(positionsRelatives[coupleCapteurs[i][1]]);
//		log.debug("distance: "+coupleCapteurs[0][2]);

		config.set(ConfigInfo.NB_CAPTEURS_PROXIMITE, nbCapteurs);
		config.set(ConfigInfo.NB_COUPLES_CAPTEURS_PROXIMITE, nbCouples);

		// Pour test
		config.set(ConfigInfo.NB_CAPTEURS_PROXIMITE, 8);
		config.set(ConfigInfo.NB_COUPLES_CAPTEURS_PROXIMITE, 4);

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
		return tmp.dot(cones[nbCapteur][0]) > 0 && tmp.dot(cones[nbCapteur][1]) > 0 && tmp.dot(cones[nbCapteur][2]) > 0 && tmp.squaredLength() < horizonCapteursSquared;
	}
	
	public boolean canBeSeenLight(Vec2<ReadOnly> point, int nbCapteur) {
		Vec2<ReadOnly> tmp = point.minusNewVector(positionsRelatives[nbCapteur]).getReadOnly();
		return tmp.dot(cones[nbCapteur][0]) > 0 && tmp.squaredLength() < horizonCapteursSquared;
	}
	
	/**
	 * Si un obstacle est vu, par quelle côté?
	 * ATTENTION: on suppose que l'obstacle est vu
	 * 1: celui de droite
	 * 2: celui de gauche
	 * @return
	 */
	public int whichSee(Vec2<ReadOnly> point, int nbCapteur)
	{
		Vec2<ReadOnly> tmp = point.minusNewVector(positionsRelatives[nbCapteur]).getReadOnly();
		if(tmp.dot(cones[nbCapteur][1]) > 0)
			return 1;
		return 2;
	}

	/**
	 * Ce point peut-il être vu par le cone arrière?
	 * Le point est dans le référentiel du robot
	 * @param point
	 * @param nbCapteur
	 * @return
	 */
	public boolean canBeSeenArriere(Vec2<ReadOnly> point, int nbCapteur, int radius)
	{
//		log.debug("Position cone: "+positionsRelatives[nbCapteur]);
//		log.debug("Diff position cone arrière: "+new Vec2<ReadWrite>(-(int)(radius/sin[nbCapteur<nbUltrasons?ultrason:infrarouge]), orientationsRelatives[nbCapteur]));
//		log.debug("Position cone arrière: "+Vec2.plus(new Vec2<ReadWrite>(-(int)(radius/sin[nbCapteur<nbUltrasons?ultrason:infrarouge]), orientationsRelatives[nbCapteur]), positionsRelatives[nbCapteur]));
		log.debug("Point avant: "+point);
		Vec2<ReadOnly> tmp = Vec2.plus(Vec2.minus(new Vec2<ReadWrite>((int)(radius/sin[nbCapteur<nbUltrasons?ultrason:infrarouge]), orientationsRelatives[nbCapteur]), positionsRelatives[nbCapteur]), point).getReadOnly();
		log.debug("Point après: "+tmp);
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
		Vec2<ReadWrite> cote;
		if(gauche)
			cote = new Vec2<ReadWrite>(distance, orientationsRelatives[capteurQuiNeVoitPas]-angleCone[capteurQuiNeVoitPas<nbUltrasons?ultrason:infrarouge]);
		else
			cote = new Vec2<ReadWrite>(distance, orientationsRelatives[capteurQuiNeVoitPas]+angleCone[capteurQuiNeVoitPas<nbUltrasons?ultrason:infrarouge]);
		
//		log.debug("cote: "+cote);
		
		Vec2.plus(cote, positionsRelatives[capteurQuiNeVoitPas]);
		Vec2.minus(cote, positionsRelatives[capteurQuiVoit]);
		double cos = 1-cote.squaredLength()/(2.*distance*distance);
		double b = -2.*distance*cos;
		double c = distance*distance - mesure*mesure;
		double delta = b*b-4*c;
		
//		log.debug("gauche: "+gauche);
//		log.debug("cos: "+cos);
//		log.debug("distance entre capteurs: "+distance);
//		log.debug("mesure: "+mesure);

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
			intersectionPoint = new Vec2<ReadWrite>((int)s, orientationsRelatives[capteurQuiNeVoitPas]-angleCone[capteurQuiVoit<nbUltrasons?ultrason:infrarouge]);
		else
			intersectionPoint = new Vec2<ReadWrite>((int)s, orientationsRelatives[capteurQuiNeVoitPas]+angleCone[capteurQuiVoit<nbUltrasons?ultrason:infrarouge]);

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
		
		Vec2.minus(intersectionPoint, positionsRelatives[capteurQuiVoit]);
		if(gauche)
			Vec2.plus(intersectionPoint, new Vec2<ReadWrite>(mesure, orientationsRelatives[capteurQuiVoit]-angleCone[capteurQuiVoit<nbUltrasons?ultrason:infrarouge]));
		else
			Vec2.plus(intersectionPoint, new Vec2<ReadWrite>(mesure, orientationsRelatives[capteurQuiVoit]+angleCone[capteurQuiVoit<nbUltrasons?ultrason:infrarouge]));
		Vec2.scalar(intersectionPoint, 0.5);

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
//		Vec2<ReadWrite> cone1 = cone[nbCapteur];
		// TODO
		return 0;
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		horizonCapteursSquared = config.getInt(ConfigInfo.HORIZON_CAPTEURS);
		horizonCapteursSquared *= horizonCapteursSquared;
	}

	public double getAngleCone(int nbCapteur)
	{
		return angleCone[nbCapteur < nbUltrasons ? ultrason : infrarouge];
	}
	
}
