package obstacles;

import obstacles.types.ObstacleProximity;
import container.Service;
import pathfinding.dstarlite.GridSpace;
import serie.Ticket;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2;
import utils.permissions.ReadOnly;
import utils.permissions.ReadWrite;

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
	private Ticket ticket;

	// Il y a seulement 4 capteurs de détection d'ennemi
	private static final int nbCapteurs = 2;
	
	private int rayonEnnemi;
    private int horizonCapteurs;

	/**
	 * Les infrarouges ont un cône de 5°
	 */
//	private final double angleConeIR = 5.*Math.PI/180.;
	
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
		 * Définition des ultrasons
		 */
				
		positionsRelatives[0] = new Vec2<ReadOnly>(70, -25);
		positionsRelatives[1] = new Vec2<ReadOnly>(70, 75);

//		positionsRelatives[2] = new Vec2<ReadOnly>(-90, -20);
//		positionsRelatives[3] = new Vec2<ReadOnly>(-90, 20);

		orientationsRelatives[0] = 0;
		orientationsRelatives[1] = 0;

//		orientationsRelatives[2] = Math.PI;
//		orientationsRelatives[3] = Math.PI;

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
	}

	/**
	 * Met à jour les obstacles mobiles
	 */
	public void updateObstaclesMobiles(IncomingData data)
	{
		double orientationRobot = data.cinematique.orientation;
		Vec2<ReadOnly> positionRobot = data.cinematique.getPosition();
		
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
//				if(Config.debugCapteurs)
//					log.debug("Capteur "+i);
				if(data.mesures[i] < 40 || data.mesures[i] > horizonCapteurs)
				{
//					if(Config.debugCapteurs)
//						log.debug("Capteur "+i+" trop proche ou trop loin.");
					continue;
				}

				Vec2<ReadWrite> positionEnnemi = new Vec2<ReadWrite>(data.mesures[i]+rayonEnnemi, orientationsRelatives[i], true);
				Vec2.plus(positionEnnemi, positionsRelatives[i]);
				Vec2.rotate(positionEnnemi, orientationRobot);
				Vec2.plus(positionEnnemi, positionRobot);
				
				if(positionEnnemi.x > 1500 || positionEnnemi.x < -1500 || positionEnnemi.y > 2000 || positionEnnemi.y < 0)
					continue; // hors table
				
/*				if(data.mesures[i] < distanceUrgence)
				{
					if(Config.debugCapteurs)
						log.warning("Ennemi !");
					// TODO
//					requete.set(RequeteType.ENNEMI_SUR_CHEMIN);
				}*/
				
//				if(Config.debugCapteurs)
//					log.debug("Obstacle vu par un capteur: "+positionEnnemi);
				ObstacleProximity o = gridspace.addObstacle(positionEnnemi.getReadOnly(), true);
				
//			    for(GameElementNames g: GameElementNames.values)
//			        if(gridspace.isDoneTable(g) == Tribool.FALSE && moteur.didTheEnemyTakeIt(g, o))
//			        	gridspace.setDoneTable(g, Tribool.MAYBE);						
			}
		}
	}
	
}
