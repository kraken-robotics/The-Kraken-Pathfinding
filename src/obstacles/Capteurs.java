package obstacles;

import obstacles.types.ObstacleProximity;
import buffer.IncomingData;
import container.Service;
import enums.Tribool;
import pathfinding.dstarlite.GridSpace;
import permissions.ReadOnly;
import permissions.ReadWrite;
import table.GameElementNames;
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
	private MoteurPhysique moteur;
	private GridSpace gridspace;
	
	private static final int nbCapteurs = 12;
	
	private static final boolean debug = true;
	
	private int squaredDistanceUrgence;
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
	 * L'orientation des capteurs lorsque le robot a une orientation nulle
	 */
	public double[] orientationsRelatives;

	public Capteurs(Log log, GridSpace gridspace, MoteurPhysique moteur)
	{
		this.log = log;
		this.gridspace = gridspace;
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

		

	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		rayonEnnemi = config.getInt(ConfigInfo.RAYON_ROBOT_ADVERSE);
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
		double orientationRobot = data.orientationRobot;
		Vec2<ReadOnly> positionRobot = data.positionRobot;
		
		/**
		 * On prend le contrôle de gridspace. Ainsi, la mise à jour du pathfinding se fera quand tous les obstacles auront été ajoutés
		 */
		synchronized(gridspace)
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
					if(debug)
						log.debug("Capteur "+i+" trop proche ou trop loin.");
					continue;
				}

				Vec2<ReadWrite> positionEnnemi = new Vec2<ReadWrite>(data.mesures[i]+rayonEnnemi, orientationsRelatives[i], true);
				Vec2.plus(positionEnnemi, positionsRelatives[i]);
				Vec2.rotate(positionEnnemi, orientationRobot);
				Vec2.plus(positionEnnemi, positionRobot);
				if(debug)
					log.debug("Obstacle vu par un capteur: "+positionEnnemi);
				ObstacleProximity o = gridspace.addObstacle(positionEnnemi.getReadOnly(), true);
			    for(GameElementNames g: GameElementNames.values)
			        if(gridspace.isDoneTable(g) == Tribool.FALSE && moteur.didTheEnemyTakeIt(g, o))
			        	gridspace.setDoneTable(g, Tribool.MAYBE);						
			}
		}
	}
	
}
