package obstacles;

import buffer.IncomingData;
import container.Service;
import permissions.ReadOnly;
import permissions.ReadWrite;
import robot.RobotReal;
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
	private RobotReal robot;

	private static final int nbCapteurs = 4;
	
	private static final boolean debug = true;
	
	private int squaredDistanceUrgence;
    private int distanceApproximation;
	private int table_x;
	private int table_y;
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
	private final Vec2<ReadOnly>[] positionsRelatives;
	
	/**
	 * Premier indice: numéro d'un capteur
	 * Deuxième indice: numéro d'une droite qui définit le cone
	 */
	private final Vec2<ReadOnly>[][] cones;
	
	/**
	 * L'orientation des capteurs lorsque le robot a une orientation nulle
	 */
	private double[] orientationsRelatives;

	public Capteurs(Log log, Config config, ObstaclesMemory memory, RobotReal robot)
	{
		this.log = log;
		this.memory = memory;
		this.robot = robot;
		positionsRelatives = new Vec2[nbCapteurs];
		orientationsRelatives = new double[nbCapteurs];

		positionsRelatives[0] = new Vec2<ReadOnly>(100, 100);
		positionsRelatives[1] = new Vec2<ReadOnly>(-100, 100);
		positionsRelatives[2] = new Vec2<ReadOnly>(-100, -100);
		positionsRelatives[3] = new Vec2<ReadOnly>(100, -100);

		double angleDeBase = - Math.PI/4 + 2*angleConeIR;
		
		orientationsRelatives[0] = -angleDeBase;
		orientationsRelatives[1] = angleDeBase;

		orientationsRelatives[2] = -angleDeBase + Math.PI/2;
		orientationsRelatives[3] = angleDeBase + Math.PI/2;

		cones = new Vec2[nbCapteurs][3];
		for(int i = 0; i < nbCapteurs; i++)
		{
			cones[i][0] = new Vec2<ReadOnly>(orientationsRelatives[i]);
			cones[i][1] = new Vec2<ReadOnly>(orientationsRelatives[i]+Math.PI/2-angleConeIR);
			cones[i][2] = new Vec2<ReadOnly>(orientationsRelatives[i]-Math.PI/2+angleConeIR);
		}
		
	}
	
	/**
	 * Ce point peut-il être vu par ce capteur?
	 * Le point est dans le référentiel du robot
	 * @param point
	 * @param nbCapteur
	 * @return
	 */
	private boolean canBeSeen(Vec2<ReadOnly> point, int nbCapteur)
	{
		Vec2<ReadOnly> tmp = point.minusNewVector(positionsRelatives[nbCapteur]).getReadOnly();
		return tmp.dot(cones[nbCapteur][0]) > 0 && tmp.dot(cones[nbCapteur][1]) > 0 && tmp.dot(cones[nbCapteur][2]) > 0 && tmp.squaredLength() < horizonCapteursSquared;
	}
	
	@Override
	public void updateConfig(Config config)
	{}

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
					Vec2<ReadWrite> positionEnnemi = new Vec2<ReadWrite>(data.mesures[i]+rayonEnnemi, orientationsRelatives[i], true);
					Vec2.plus(positionEnnemi, positionsRelatives[i]);
					Vec2.rotate(positionEnnemi, orientationRobot);
					Vec2.plus(positionEnnemi, positionRobot);
					if(debug)
						log.debug("Obstacle vu : "+positionEnnemi);
					memory.add(positionEnnemi.getReadOnly(), System.currentTimeMillis(), isUrgent(positionEnnemi.getReadOnly(), positionRobot, orientationRobotAvance));
	
				}
					
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
