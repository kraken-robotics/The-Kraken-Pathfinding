package obstacles;

import obstacles.types.Obstacle;
import obstacles.types.ObstacleCircular;
import obstacles.types.ObstacleRectangular;
import buffer.IncomingData;
import container.Service;
import permissions.ReadOnly;
import permissions.ReadWrite;
import robot.RobotReal;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2;

// N'EST PAS UTILISÉ !

/**
 * Cette classe contient les informations sur la situation
 * spatiale des capteurs sur le robot.
 * @author pf
 *
 */

@SuppressWarnings("unchecked")
public class OldCapteurs implements Service {
	// DEPENDS ON ROBOT
	
	protected Log log;
	private ObstaclesMemory memory;
	private RobotReal robot;
	
	private static final int nbCapteurs = 12;
	
	private static final int nbCouples = 4;
	
	private static final boolean debug = true;
	
	private int squaredDistanceUrgence;
    private int distanceApproximation;
	private int table_x = 3000;
	private int table_y = 2000;
	private int rayonEnnemi;
    private int horizonCapteurs;

	/**
	 * Les ultrasons ont un cône de 35°
	 * Les infrarouges, de 5°
	 */
	private final double[] angleCone = {35.*Math.PI/180., 5.*Math.PI/180.};
	
	private static final int ultrason = 0;
	private static final int infrarouge = 1;
	
	private static final int nbUltrasons = 12;
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
	public int[][] coupleCapteurs;

	/**
	 * L'orientation des capteurs lorsque le robot a une orientation nulle
	 */
	public double[] orientationsRelatives;

	public OldCapteurs(Log log, Config config, ObstaclesMemory memory, RobotReal robot)
	{
		this.log = log;
		this.memory = memory;
		this.robot = robot;
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

 //		config.set(ConfigInfo.NB_CAPTEURS_PROXIMITE, nbCapteurs);
//		config.set(ConfigInfo.NB_COUPLES_CAPTEURS_PROXIMITE, nbCouples);

		// Pour test
//		config.set(ConfigInfo.NB_CAPTEURS_PROXIMITE, 8);
//		config.set(ConfigInfo.NB_COUPLES_CAPTEURS_PROXIMITE, 4);

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
	
	private boolean canBeSeenLight(Vec2<ReadOnly> point, int nbCapteur) {
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
	private int whichSee(Vec2<ReadOnly> point, int nbCapteur)
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
	private boolean canBeSeenArriere(Vec2<ReadOnly> point, int nbCapteur, int radius)
	{
//		log.debug("Position cone: "+positionsRelatives[nbCapteur]);
//		log.debug("Diff position cone arrière: "+new Vec2<ReadWrite>(-(int)(radius/sin[nbCapteur<nbUltrasons?ultrason:infrarouge]), orientationsRelatives[nbCapteur]));
//		log.debug("Position cone arrière: "+Vec2.plus(new Vec2<ReadWrite>(-(int)(radius/sin[nbCapteur<nbUltrasons?ultrason:infrarouge]), orientationsRelatives[nbCapteur]), positionsRelatives[nbCapteur]));
		log.debug("Point avant: "+point);
		Vec2<ReadOnly> tmp = Vec2.plus(Vec2.minus(new Vec2<ReadWrite>((int)(radius/sin[nbCapteur<nbUltrasons?ultrason:infrarouge]), orientationsRelatives[nbCapteur], true), positionsRelatives[nbCapteur]), point).getReadOnly();
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
	
	private Vec2<ReadWrite> getPositionAjustee(int nbCouple, boolean gauche, int mesure)
	{
		int capteurQuiVoit = coupleCapteurs[nbCouple][gauche?0:1];
		int capteurQuiNeVoitPas = coupleCapteurs[nbCouple][gauche?1:0];
		Vec2<ReadWrite> intersectionPoint;
		int distance = coupleCapteurs[nbCouple][2];
		Vec2<ReadWrite> cote;
		if(gauche)
			cote = new Vec2<ReadWrite>(distance, orientationsRelatives[capteurQuiNeVoitPas]-angleCone[capteurQuiNeVoitPas<nbUltrasons?ultrason:infrarouge], true);
		else
			cote = new Vec2<ReadWrite>(distance, orientationsRelatives[capteurQuiNeVoitPas]+angleCone[capteurQuiNeVoitPas<nbUltrasons?ultrason:infrarouge], true);
		
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
			intersectionPoint = new Vec2<ReadWrite>((int)s, orientationsRelatives[capteurQuiNeVoitPas]-angleCone[capteurQuiVoit<nbUltrasons?ultrason:infrarouge], true);
		else
			intersectionPoint = new Vec2<ReadWrite>((int)s, orientationsRelatives[capteurQuiNeVoitPas]+angleCone[capteurQuiVoit<nbUltrasons?ultrason:infrarouge], true);

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
			Vec2.plus(intersectionPoint, new Vec2<ReadWrite>(mesure, orientationsRelatives[capteurQuiVoit]-angleCone[capteurQuiVoit<nbUltrasons?ultrason:infrarouge], true));
		else
			Vec2.plus(intersectionPoint, new Vec2<ReadWrite>(mesure, orientationsRelatives[capteurQuiVoit]+angleCone[capteurQuiVoit<nbUltrasons?ultrason:infrarouge], true));
		Vec2.scalar(intersectionPoint, 0.5);

		return intersectionPoint;
	}
	
	/**
	 * A quelle distance le capteur voit-il cet obstacle?
	 * @param nbCapteur
	 * @param o
	 * @return
	 */
//	private int distanceSeen(int nbCapteur, ObstacleCircular o)
//	{
//		Vec2<ReadWrite> cone1 = cone[nbCapteur];
//		// TODO
//		return 0;
//	}

	@Override
	public void updateConfig(Config config)
	{
//		nbCapteurs = config.getInt(ConfigInfo.NB_CAPTEURS_PROXIMITE);
//		nbCouples = config.getInt(ConfigInfo.NB_COUPLES_CAPTEURS_PROXIMITE);
	}

	@Override
	public void useConfig(Config config)
	{
		table_x = config.getInt(ConfigInfo.TABLE_X);
		table_y = config.getInt(ConfigInfo.TABLE_Y);
		rayonEnnemi = config.getInt(ConfigInfo.RAYON_ROBOT_ADVERSE);
		distanceApproximation = config.getInt(ConfigInfo.DISTANCE_MAX_ENTRE_MESURE_ET_OBJET);
		horizonCapteurs = config.getInt(ConfigInfo.HORIZON_CAPTEURS);
		horizonCapteursSquared = config.getInt(ConfigInfo.HORIZON_CAPTEURS);
		horizonCapteursSquared *= horizonCapteursSquared;
		squaredDistanceUrgence = config.getInt(ConfigInfo.DISTANCE_URGENCE);
		squaredDistanceUrgence *= squaredDistanceUrgence;
	}

	private double getAngleCone(int nbCapteur)
	{
		return angleCone[nbCapteur < nbUltrasons ? ultrason : infrarouge];
	}

	/**
	 * Met à jour les obstacles mobiles
	 */
	public void updateObstaclesMobiles(IncomingData data)
	{
		boolean[] neVoitRien = new boolean[nbCapteurs];
		boolean[] dejaTraite = new boolean[nbCapteurs];
		double orientationRobot = robot.getOrientation();
		Vec2<ReadOnly> orientationRobotAvance = new Vec2<ReadOnly>(robot.getOrientationAvance());
		Vec2<ReadOnly> positionRobot = robot.getPosition();
		
		// Ce synchronized permet d'ajouter plusieurs obstacles avant de mettre à jour le gridspace
		synchronized(memory)
		{
			/**
			 * Suppression des mesures qui sont hors-table ou qui voient un obstacle de table
			 */
			for(int i = 0; i < nbCapteurs; i++)
			{
				dejaTraite[i] = false;
				if(debug)
					log.debug("Capteur "+i);
				if(data.mesures[i] < 40 || data.mesures[i] > horizonCapteurs)
				{
					neVoitRien[i] = true;
					if(debug)
						log.debug("Capteur "+i+" trop proche ou trop loin.");
					continue;
				}
				Vec2<ReadWrite> positionBrute = new Vec2<ReadWrite>(data.mesures[i], orientationsRelatives[i], true);
				Vec2.plus(positionBrute, positionsRelatives[i]);
				Vec2.rotate(positionBrute, orientationRobot);
				Vec2.plus(positionBrute, positionRobot);
				if(debug)
					log.debug("Position brute: "+positionBrute);
				
				if(positionBrute.x > table_x / 2 - distanceApproximation ||
						positionBrute.x < -table_x / 2 + distanceApproximation ||
						positionBrute.y < distanceApproximation ||
						positionBrute.y > table_y - distanceApproximation/* ||
						isObstacleFixePresentCapteurs(positionBrute.getReadOnly())*/)
				{
					if(debug)
						log.debug("Capteur "+i+" ignoré car hors-table.");
					neVoitRien[i] = true; // le capteur voit un obstacle fixe: on ignore sa valeur
				}
				else
				{
					/**
					 * Si un capteur voit un obstacle de table, alors on l'ignore
					 */
					for(ObstaclesFixes o: ObstaclesFixes.obstaclesFixesVisibles)
					{
						Obstacle obs = o.getObstacle();
						if(debug)
							log.debug("Vérification obstacle en "+obs.position);
	
						if(obs instanceof ObstacleCircular)
						{
							if(debug)
								log.debug("Obstacle circulaire");
	
							ObstacleCircular obsc = (ObstacleCircular) obs;
							
							/**
							 * positionObstacle est la position de l'obstacle dans le repère du robot
							 */
							Vec2<ReadOnly> positionObstacle = Vec2.rotate(obsc.position.minusNewVector(positionRobot), -orientationRobot).getReadOnly();
							
							if(debug)
								log.debug("Position obstacle dans le repère du robot: "+positionObstacle);
	
							if(canBeSeenArriere(positionObstacle, i, ((ObstacleCircular) obs).radius))
							{
								/**
								 * On voit l'obstacle circulaire de table. Reste maintenant
								 * à savoir à quelle distance on devrait le voir afin de savoir
								 * si c'est vraiment lui qu'on voit
								 */
								int distance;
								if(canBeSeen(positionObstacle, i))
								{
									/**
									 * Cas simple: on voit le centre du cercle
									 */
									if(debug)
										log.debug("Cas simple");
									distance = (int) (positionObstacle.distance(positionsRelatives[i]) - obsc.radius);
								}
								else
								{
									/**
									 * Cas plus complexe: on voit seulement un bout du cercle
									 */
									if(debug)
										log.debug("Cas complexe");
									int cote = whichSee(positionObstacle, i);
									if(debug)
										log.debug("Côté qui voit: "+cote);
									distance = (int)ObstacleCircular.getDistance(positionObstacle, obsc.radius, positionsRelatives[i], cones[3-i][cote]);
								}
								
								if(debug)
									log.debug("Distance prédite pour "+i+": "+distance);
								
								if(Math.abs(distance - data.mesures[i]) < distanceApproximation)
								{
									if(debug)
										log.debug("Le capteur "+i+" voit un obstacle fixe circulaire");
									dejaTraite[i] = true;
									// On a trouvé quel obstacle on voyait, pas besoin d'aller plus loin
									break;
								}
								
							}
							else if(debug)
								log.debug("Obstacle pas visible avec cone arrière");
						}
						else if(obs instanceof ObstacleRectangular)
						{
							if(debug)
								log.debug("Obstacle rectangulaire");
	
							ObstacleRectangular obsr = (ObstacleRectangular) obs;
	
							if(debug)
								log.debug("Vérification obstacle en "+obsr.position);
	
							/**
							 * On vérifie d'abord quel coin sont visibles ou non
							 */
							boolean coinBasDroiteVisible, coinBasGaucheVisible, coinHautDroiteVisible, coinHautGaucheVisible;
							coinBasDroiteVisible = canBeSeen(Vec2.rotate(obsr.coinBasDroite.minusNewVector(positionRobot), -orientationRobot).getReadOnly(), i);
							coinBasGaucheVisible = canBeSeen(Vec2.rotate(obsr.coinBasGauche.minusNewVector(positionRobot), -orientationRobot).getReadOnly(), i);
							coinHautDroiteVisible = canBeSeen(Vec2.rotate(obsr.coinHautDroite.minusNewVector(positionRobot), -orientationRobot).getReadOnly(), i);
							coinHautGaucheVisible = canBeSeen(Vec2.rotate(obsr.coinHautGauche.minusNewVector(positionRobot), -orientationRobot).getReadOnly(), i);
							
							int distance;
							Vec2<ReadOnly> coinPlusProcheVisible = obsr.getPlusProcheCoinVisible(positionRobot, coinBasDroiteVisible, coinBasGaucheVisible, coinHautDroiteVisible, coinHautGaucheVisible);
							if(coinPlusProcheVisible != null || coinPlusProcheVisible == obsr.getPlusProcheCoinVisible(positionRobot, true, true, true, true))
							{
								/**
								 * Le coin le plus proche du robot est visible.
								 * Alors c'est la partie du rectangle la plus proche du capteur.
								 */
								if(debug)
									log.debug("Cas simple");
								distance = (int) Vec2.rotate(coinPlusProcheVisible.minusNewVector(positionRobot), -orientationRobot).getReadOnly().distance(positionsRelatives[i]);
							}
							else
							{
								distance = Integer.MAX_VALUE;
	
								/**
								 * Le point le plus proche des capteurs est sur une arête du rectangle
								 */
								if(debug)
									log.debug("Cas complexe");
	
								/**
								 * Cas où le plus proche point n'est pas dans un côté du cône
								 */
	
								Vec2<ReadWrite> coinBasDroiteRepereRobotSansRotation = obsr.coinBasDroite.minusNewVector(positionRobot);
								
								Vec2<ReadWrite> point;
								
								point = Vec2.rotate(new Vec2<ReadWrite>(0, coinBasDroiteRepereRobotSansRotation.y), -orientationRobot);
								
								if(canBeSeen(point.getReadOnly(), i))
								{
									distance = Math.min(distance, Math.abs(coinBasDroiteRepereRobotSansRotation.y));
								}
								
								point = Vec2.rotate(new Vec2<ReadWrite>(coinBasDroiteRepereRobotSansRotation.x, 0), -orientationRobot);
								
								if(canBeSeen(point.getReadOnly(), i))
								{
									distance = Math.min(distance, Math.abs(coinBasDroiteRepereRobotSansRotation.x));
								}
	
								Vec2<ReadWrite> coinBasGaucheRepereRobotSansRotation = obsr.coinBasGauche.minusNewVector(positionRobot);
								
								point = Vec2.rotate(new Vec2<ReadWrite>(coinBasGaucheRepereRobotSansRotation.x, 0), -orientationRobot);
								
								if(canBeSeen(point.getReadOnly(), i))
								{
									distance = Math.min(distance, Math.abs(coinBasGaucheRepereRobotSansRotation.x));
								}
	
								Vec2<ReadWrite> coinHautDroiteRepereRobotSansRotation = obsr.coinHautDroite.minusNewVector(positionRobot);
								
								point = Vec2.rotate(new Vec2<ReadWrite>(0, coinHautDroiteRepereRobotSansRotation.y), -orientationRobot);
								
								if(canBeSeen(point.getReadOnly(), i))
								{
									distance = Math.min(distance, Math.abs(coinHautDroiteRepereRobotSansRotation.y));
								}
	
								/**
								 * Cas où le plus proche point est sur une arête du cône
								 */
								
								double tanAlpha1 = orientationRobot + orientationsRelatives[i];
								double tanAlpha2 = tanAlpha1 - getAngleCone(i);
								tanAlpha1 += getAngleCone(i);
								
								tanAlpha1 = Math.tan(tanAlpha1);
								tanAlpha2 = Math.tan(tanAlpha2);
								
								int x, y;
								Vec2<ReadWrite> p;
	
								/**
								 * Tangente positive pour le côté gauche du cône:
								 * il faut vérifier qu'on ne croise pas un côté horizontal de l'obstacle
								 */
								
								if(tanAlpha1 > 0)
								{
									y = obsr.coinBasDroite.y - positionRobot.y - positionsRelatives[i].y;
									x = (int)(y/tanAlpha1);
									p = new Vec2<ReadWrite>(x, y);
									Vec2.plus(p, positionsRelatives[i]);
									if(canBeSeen(p.getReadOnly(), i))
										distance = Math.min(distance, Math.abs(coinBasGaucheRepereRobotSansRotation.x));
	
									y = obsr.coinHautDroite.y - positionRobot.y - positionsRelatives[i].y;
									x = (int)(y/tanAlpha1);
									p = new Vec2<ReadWrite>(x, y);
									Vec2.plus(p, positionsRelatives[i]);
									if(canBeSeen(p.getReadOnly(), i))
										distance = Math.min(distance, Math.abs(coinBasGaucheRepereRobotSansRotation.x));
								}
								else
								{
									x = obsr.coinBasDroite.x - positionRobot.x - positionsRelatives[i].x;
									y = (int)(x*tanAlpha1);
									p = new Vec2<ReadWrite>(x, y);
									Vec2.plus(p, positionsRelatives[i]);
									if(canBeSeen(p.getReadOnly(), i))
										distance = Math.min(distance, Math.abs(coinBasGaucheRepereRobotSansRotation.x));
	
									x = obsr.coinHautGauche.x - positionRobot.x - positionsRelatives[i].x;
									y = (int)(x*tanAlpha1);
									p = new Vec2<ReadWrite>(x, y);
									Vec2.plus(p, positionsRelatives[i]);
									if (canBeSeen(p.getReadOnly(), i))
										distance = Math.min(distance, Math.abs(coinBasGaucheRepereRobotSansRotation.x));
								}
								
								if(tanAlpha2 >= 0)
								{
									x = obsr.coinBasDroite.x - positionRobot.x - positionsRelatives[i].x;
									y = (int)(x*tanAlpha1);
									p = new Vec2<ReadWrite>(x, y);
									Vec2.plus(p, positionsRelatives[i]);
									if(canBeSeen(p.getReadOnly(), i))
										distance = Math.min(distance, Math.abs(coinBasGaucheRepereRobotSansRotation.x));
	
									x = obsr.coinHautGauche.x - positionRobot.x - positionsRelatives[i].x;
									y = (int)(x*tanAlpha1);
									p = new Vec2<ReadWrite>(x, y);
									Vec2.plus(p, positionsRelatives[i]);
									if(canBeSeen(p.getReadOnly(), i))
										distance = Math.min(distance, Math.abs(coinBasGaucheRepereRobotSansRotation.x));
								}
								else
								{
									y = obsr.coinBasDroite.y - positionRobot.y - positionsRelatives[i].y;
									x = (int)(y/tanAlpha2);
									p = new Vec2<ReadWrite>(x, y);
									Vec2.plus(p, positionsRelatives[i]);
									if(canBeSeen(p.getReadOnly(), i))
										distance = Math.min(distance, Math.abs(coinBasGaucheRepereRobotSansRotation.x));
	
									y = obsr.coinHautDroite.y - positionRobot.y - positionsRelatives[i].y;
									x = (int)(y/tanAlpha2);
									p = new Vec2<ReadWrite>(x, y);
									Vec2.plus(p, positionsRelatives[i]);
									if(canBeSeen(p.getReadOnly(), i))
										distance = Math.min(distance, Math.abs(coinBasGaucheRepereRobotSansRotation.x));
								}
								
							}
							
							if(Math.abs(distance - data.mesures[i]) < distanceApproximation)
							{
								if(debug)
									log.debug("Le capteur "+i+" voit un obstacle fixe rectangulaire");
								dejaTraite[i] = true;
								// On a trouvé quel obstacle on voyait, pas besoin d'aller plus loin
								break;
							}
	
						}
					}
	//				log.debug("Ok");
					neVoitRien[i] = false;
					data.mesures[i] += rayonEnnemi;
				}
			}
			
			/**
			 * On cherche les détections couplées en priorité
			 */
			for(int i = 0; i < nbCouples; i++)
			{
				int nbCapteur1 = coupleCapteurs[i][0];
				int nbCapteur2 = coupleCapteurs[i][1];
				
				if(debug)
					log.debug("nbCapteur1: "+nbCapteur1);
				if(debug)
					log.debug("nbCapteur2: "+nbCapteur2);
	
				if((neVoitRien[nbCapteur1] || dejaTraite[nbCapteur1]) && (neVoitRien[nbCapteur2] || dejaTraite[nbCapteur2]))
				{
					if(debug)
						log.debug("Couple "+i+": déjà fait");
					continue;
				}
				else if((neVoitRien[nbCapteur1] ^ neVoitRien[nbCapteur2]) && (!dejaTraite[nbCapteur1] && !dejaTraite[nbCapteur2]))
				{
					if(debug)
						log.debug("Un capteur voit et pas l'autre");
					/**
					 * Cas où un capteur voit et pas l'autre
					 */
					int nbCapteurQuiVoit = neVoitRien[nbCapteur2]?nbCapteur1:nbCapteur2;
					Vec2<ReadWrite> pointVu = getPositionAjustee(i, neVoitRien[nbCapteur2], data.mesures[nbCapteurQuiVoit]);
					if(pointVu == null)
					{
						if(debug)
							log.debug("Point vu: null");
						continue;
					}
					Vec2.plus(pointVu, positionsRelatives[nbCapteurQuiVoit]);
					Vec2.rotate(pointVu, orientationRobot);
					Vec2.plus(pointVu, positionRobot);
					memory.add(pointVu.getReadOnly(), System.currentTimeMillis(), isUrgent(pointVu.getReadOnly(), positionRobot, orientationRobotAvance));
					neVoitRien[nbCapteurQuiVoit] = true;
				}
				else if(!dejaTraite[nbCapteur1] && !dejaTraite[nbCapteur2] && !neVoitRien[nbCapteur1] && !neVoitRien[nbCapteur2])
				{	
					if(debug)
						log.debug("Deux capteurs voient");
					/**
					 * Cas où les deux capteurs voient
					 */
					int distanceEntreCapteurs = coupleCapteurs[i][2];
					int mesure1 = data.mesures[nbCapteur1];
					int mesure2 = data.mesures[nbCapteur2];
					
					// Si l'inégalité triangulaire n'est pas respectée
					if(mesure1 + mesure2 <= distanceEntreCapteurs)
					{
						if(debug)
							log.debug("Inégalité triangulaire non respectée");
						continue;
					}
					
					double posX = ((double)(distanceEntreCapteurs*distanceEntreCapteurs + mesure1*mesure1 - mesure2*mesure2))/(2*distanceEntreCapteurs);
					double posY = Math.sqrt(mesure1*mesure1 - posX*posX);
					
					Vec2<ReadWrite> pointVu1 = positionsRelatives[nbCapteur2].clone();
					Vec2.minus(pointVu1, positionsRelatives[nbCapteur1]);
					Vec2<ReadWrite> BC = pointVu1.clone();
					Vec2.rotateAngleDroit(BC);
					Vec2.scalar(BC, posY/distanceEntreCapteurs);
					if(debug)
						log.debug("Longueur BC: "+BC.length()+", posY: "+posY);
					Vec2.scalar(pointVu1, (double)(posX)/distanceEntreCapteurs);
					Vec2.plus(pointVu1, positionsRelatives[nbCapteur1]);
					Vec2<ReadWrite> pointVu2 = pointVu1.clone();
					Vec2.plus(pointVu1, BC);
					Vec2.minus(pointVu2, BC);
					
					/**
					 * Il y a deux points, pointVu1 et pointVu2 car l'intersection
					 * de deux cercles a deux solutions: une devant les capteurs, une derrière
					 */
					
					if(debug)
						log.debug("Point vu 1: "+pointVu1);
					if(debug)
						log.debug("Point vu 2: "+pointVu2);
					
					/**
					 * Afin de départager ces deux points, on regarde lequel est visible par les 
					 * Sauf qu'à cause du bruit, il est possible que le bon point ne soit pas visible mais légèrement en dehors...
					 * Du coup, on utilise une version allégée de "canBeSeen", qui vérifie juste que le côté est bon.
					 */
					
					boolean vu = canBeSeenLight(pointVu1.getReadOnly(), nbCapteur1) && canBeSeenLight(pointVu1.getReadOnly(), nbCapteur2);
					if(debug)
					{
						if(vu)
							log.debug("pointVu1 est visible");
						else
							log.debug("pointVu1 n'est pas visible!");
					}
		
					if(!vu)
					{
						vu = canBeSeenLight(pointVu2.getReadOnly(), nbCapteur1) && canBeSeenLight(pointVu2.getReadOnly(), nbCapteur2);
						pointVu1 = pointVu2;
						Vec2.oppose(BC);
						if(debug)
						{
							if(vu)
								log.debug("pointVu2 est visible");
							else
								log.debug("pointVu2 n'est pas visible!");
						}
					}
					
					if(vu)			
					{
						neVoitRien[nbCapteur1] = true;
						neVoitRien[nbCapteur2] = true;
						if(debug)
							log.debug("Scalaire: "+(rayonEnnemi)/posY);
	//					Vec2.scalar(BC, ((double)rayonEnnemi)/posY);
	//					Vec2.plus(pointVu1, BC);
						Vec2.rotate(pointVu1, orientationRobot);
						Vec2.plus(pointVu1, positionRobot);
						if(debug)
							log.debug("Longueur BC: "+BC.length());
						// TODO: supprimer tous les autres obstacles près de pointVu1
						memory.add(pointVu1.getReadOnly(), System.currentTimeMillis(), isUrgent(pointVu1.getReadOnly(), positionRobot, orientationRobotAvance));
					}
				}
			}
			
			/**
			 * Maintenant, on récupère tous les capteurs qui n'ont pas participé à une détection couplée
			 */
			for(int i = 0; i < nbCapteurs; i++)
				if(!neVoitRien[i] && !dejaTraite[i])
				{
					Vec2<ReadWrite> positionEnnemi = new Vec2<ReadWrite>(data.mesures[i]+rayonEnnemi, orientationsRelatives[i], true);
					Vec2.plus(positionEnnemi, positionsRelatives[i]);
					Vec2.rotate(positionEnnemi, orientationRobot);
					Vec2.plus(positionEnnemi, positionRobot);
					if(debug)
						log.debug("Obstacle vu par un seul capteur: "+positionEnnemi);
					memory.add(positionEnnemi.getReadOnly(), System.currentTimeMillis(), isUrgent(positionEnnemi.getReadOnly(), positionRobot, orientationRobotAvance));
	
				}
		}
	}
	
	/**
	 * Il y a urgence si l'ennemi est proche et dans le sens de la marche			
	 * @param positionEnnemi
	 * @param positionRobot
	 * @param angleAvance
	 * @return
	 */
	private final boolean isUrgent(Vec2<ReadOnly> positionEnnemi, Vec2<ReadOnly> positionRobot, Vec2<ReadOnly> angleAvance)
	{
		return positionEnnemi.squaredDistance(positionRobot) < squaredDistanceUrgence
				&& positionEnnemi.minusNewVector(positionRobot).dot(angleAvance) > 0;
	}
	
}
