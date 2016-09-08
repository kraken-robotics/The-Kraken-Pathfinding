package obstacles;

import obstacles.types.ObstacleProximity;
import pathfinding.dstarlite.gridspace.GridSpace;
import container.Service;
import table.GameElementNames;
import table.Table;
import table.Tribool;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2RO;
import utils.Vec2RW;

/**
 * Cette classe contient les informations sur la situation
 * spatiale des capteurs sur le robot.
 * @author pf
 *
 */

public class Capteurs implements Service {
	protected Log log;
	private GridSpace gridspace;
	private Table table;
	public static final int nbCapteurs = 2;
	
	private int rayonEnnemi;
    private int horizonCapteurs;
    private int rayonRobot;
	private int distanceApproximation;

	/**
	 * Les positions relatives des capteurs par rapport au centre du
	 * robot lorsque celui-ci a une orientation nulle.
	 */
	public final Vec2RO[] positionsRelatives;

	/**
	 * L'orientation des capteurs lorsque le robot a une orientation nulle
	 */
	public double[] orientationsRelatives;

	public Capteurs(Log log, GridSpace gridspace, Table table)
	{
		this.table = table;
		this.log = log;
		this.gridspace = gridspace;
		positionsRelatives = new Vec2RO[nbCapteurs];
		orientationsRelatives = new double[nbCapteurs];

		// TODO à compléter
		positionsRelatives[0] = new Vec2RO(70, -25);
		positionsRelatives[1] = new Vec2RO(70, 75);

		orientationsRelatives[0] = 0;
		orientationsRelatives[1] = 0;

	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		rayonEnnemi = config.getInt(ConfigInfo.RAYON_ROBOT_ADVERSE);
		rayonRobot = config.getInt(ConfigInfo.RAYON_ROBOT);
		horizonCapteurs = config.getInt(ConfigInfo.HORIZON_CAPTEURS);
		distanceApproximation = config.getInt(ConfigInfo.DISTANCE_MAX_ENTRE_MESURE_ET_OBJET);		
	}

	/**
	 * Met à jour les obstacles mobiles
	 */
	public void updateObstaclesMobiles(SensorsData data)
	{
		double orientationRobot = data.cinematique.orientation;
		Vec2RO positionRobot = data.cinematique.getPosition();
		boolean needNotify = false; // on ne notifie qu'une seule fois
		
		/**
		 * On prend le contrôle de gridspace. Ainsi, la mise à jour du pathfinding se fera quand tous les obstacles auront été ajoutés
		 */
		synchronized(gridspace)
		{
			/**
			 * On update la table avec notre position
			 */
		    for(GameElementNames g: GameElementNames.values())
		        if(g.obstacle.isProcheObstacle(positionRobot, rayonRobot))
		        	table.setDone(g, Tribool.TRUE); // on est sûr de l'avoir shooté
			
			
			/**
			 * Suppression des mesures qui sont hors-table ou qui voient un obstacle de table
			 */
			for(int i = 0; i < nbCapteurs; i++)
			{
				/**
				 * Si le capteur voit trop proche ou trop loin, on ne peut pas lui faire confiance
				 */
				if(data.mesures[i] < 40 || data.mesures[i] > horizonCapteurs)
					continue;

				/**
				 * Si ce qu'on voit est un obstacle de table, on l'ignore
				 */
				Vec2RO positionVue = new Vec2RO(data.mesures[i], orientationsRelatives[i], true);
				
		    	for(ObstaclesFixes o: ObstaclesFixes.obstaclesFixesVisibles)
		    		if(o.getObstacle().squaredDistance(positionVue) < distanceApproximation * distanceApproximation)
		                continue;
				
				/**
				 * Sinon, on ajoute
				 */
				needNotify = true;
				Vec2RW positionEnnemi = new Vec2RW(data.mesures[i]+rayonEnnemi, orientationsRelatives[i], true);
				positionEnnemi.plus(positionsRelatives[i]);
				positionEnnemi.rotate(orientationRobot);
				positionEnnemi.plus(positionRobot);
				
				if(positionEnnemi.x > 1500 || positionEnnemi.x < -1500 || positionEnnemi.y > 2000 || positionEnnemi.y < 0)
					continue; // hors table
				
				ObstacleProximity o = gridspace.addObstacleAndRemoveNearbyObstacles(positionEnnemi.getReadOnly());
				
				/**
				 * Mise à jour de l'état de la table
				 */
			    for(GameElementNames g: GameElementNames.values())
			        if(table.isDone(g) == Tribool.FALSE && g.obstacle.isProcheObstacle(o, o.radius))
			        	table.setDone(g, Tribool.MAYBE);

			}
			if(needNotify)
				notify();
		}
	}
	
}
