package obstacles;

import obstacles.types.Obstacle;
import obstacles.types.ObstacleCircular;
import obstacles.types.ObstacleProximity;
import obstacles.types.ObstacleRectangular;
import buffer.IncomingData;
import container.Service;
import enums.Tribool;
import permissions.ReadOnly;
import permissions.ReadWrite;
import table.GameElementNames;
import table.Table;
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
	protected Log log;
	private ObstaclesMemory memory;
	private Table table;
	private MoteurPhysique moteur;
	
	private static final int nbCapteurs = 12;
	
	private static final boolean debug = true;
	
	private int squaredDistanceUrgence;
    private int distanceApproximation;
	private int table_x = 3000;
	private int table_y = 2000;
	private int rayonEnnemi;
    private int horizonCapteurs;

	/**
	 * Les infrarouges ont un cône de 5°
	 */
	private final double angleConeIR = 5.*Math.PI/180.;
	
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

	private final double sinIR;
	/**
	 * L'orientation des capteurs lorsque le robot a une orientation nulle
	 */
	public double[] orientationsRelatives;

	public Capteurs(Log log, ObstaclesMemory memory, Table table, MoteurPhysique moteur)
	{
		this.log = log;
		this.memory = memory;
		this.table = table;
		this.moteur = moteur;
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
		angleDeBase = - Math.PI/4 + 2*angleConeIR;
		
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
			cones[i][1] = new Vec2<ReadOnly>(orientationsRelatives[i]+Math.PI/2-angleConeIR);
			cones[i][2] = new Vec2<ReadOnly>(orientationsRelatives[i]-Math.PI/2+angleConeIR);
		}
		
		sinIR = Math.sin(angleConeIR);		
		

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
	
	/**
	 * Si un obstacle est vu, par quel côté?
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
		Vec2<ReadOnly> tmp = Vec2.plus(Vec2.minus(new Vec2<ReadWrite>((int)(radius/sinIR), orientationsRelatives[nbCapteur], true), positionsRelatives[nbCapteur]), point).getReadOnly();
		log.debug("Point après: "+tmp);
		return tmp.dot(cones[nbCapteur][0]) > 0 && tmp.dot(cones[nbCapteur][1]) > 0 && tmp.dot(cones[nbCapteur][2]) > 0;
	}

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

	/**
	 * Met à jour les obstacles mobiles
	 */
	public void updateObstaclesMobiles(IncomingData data)
	{
		boolean[] neVoitRien = new boolean[nbCapteurs];
		boolean[] dejaTraite = new boolean[nbCapteurs];
		double orientationRobot = data.orientationRobot;
		double tmp = orientationRobot;
		if(data.enMarcheAvant)
			tmp += Math.PI;

		Vec2<ReadOnly> orientationRobotAvance = new Vec2<ReadOnly>(tmp);
		Vec2<ReadOnly> positionRobot = data.positionRobot;
		
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
						log.debug("Capteur "+i+" ignoré car hors table.");
					neVoitRien[i] = true; // le capteur quelque chose hors table: on ignore sa valeur
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
	
						/**
						 * CAS 1 : OBSTACLE CIRCULAIRE
						 */
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
									distance = (int)ObstacleCircular.getDistance(positionObstacle, obsc.radius, positionsRelatives[i], cones[i][cote]);
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

						/**
						 * CAS 1 : OBSTACLE RECTANGULAIRE
						 */
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
								 * En fait, ce n'est pas forcément vrai. Mais bon, on va faire comme si…
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
								double tanAlpha2 = tanAlpha1 - angleConeIR;
								tanAlpha1 += angleConeIR;
								
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
						
						// Si on a trouvé un obstacle de table qui correspond, pas besoin de vérifier les autres
						if(dejaTraite[i])
							break;
					}
	//				log.debug("Ok");
					neVoitRien[i] = false;
					data.mesures[i] += rayonEnnemi;
				}
			}
			
			/**
			 * Maintenant, on récupère tous les capteurs qui n'ont pas vu un obstacle
			 */
			for(int i = 0; i < nbCapteurs; i++)
				if(!neVoitRien[i] && !dejaTraite[i])
				{
					Vec2<ReadWrite> positionEnnemi = new Vec2<ReadWrite>(data.mesures[i]+rayonEnnemi, orientationsRelatives[i], true);
					Vec2.plus(positionEnnemi, positionsRelatives[i]);
					Vec2.rotate(positionEnnemi, orientationRobot);
					Vec2.plus(positionEnnemi, positionRobot);
					if(debug)
						log.debug("Obstacle vu par un capteur: "+positionEnnemi);
					ObstacleProximity o = memory.add(positionEnnemi.getReadOnly(), System.currentTimeMillis(), isUrgent(positionEnnemi.getReadOnly(), positionRobot, orientationRobotAvance));
				    for(GameElementNames g: GameElementNames.values)
				        if(table.isDone(g) == Tribool.FALSE && moteur.didTheEnemyTakeIt(g, o))
				        	table.setDone(g, Tribool.MAYBE);						
	
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
	// TODO
	private final boolean isUrgent(Vec2<ReadOnly> positionEnnemi, Vec2<ReadOnly> positionRobot, Vec2<ReadOnly> angleAvance)
	{
		return positionEnnemi.squaredDistance(positionRobot) < squaredDistanceUrgence
				&& positionEnnemi.minusNewVector(positionRobot).dot(angleAvance) > 0;
	}
	
}
