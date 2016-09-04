package obstacles;

import obstacles.types.ObstacleProximity;
import container.Service;
import enums.Tribool;
import pathfinding.dstarlite.GridSpace;
import table.GameElementNames;
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
	private GridSpace gridspace;

	public static final int nbCapteurs = 2;
	
	private int rayonEnnemi;
    private int horizonCapteurs;
	
	/**
	 * Les positions relatives des capteurs par rapport au centre du
	 * robot lorsque celui-ci a une orientation nulle.
	 */
	public final Vec2<ReadOnly>[] positionsRelatives;

	/**
	 * L'orientation des capteurs lorsque le robot a une orientation nulle
	 */
	public double[] orientationsRelatives;

	public Capteurs(Log log, GridSpace gridspace)
	{
		this.log = log;
		this.gridspace = gridspace;
		positionsRelatives = new Vec2[nbCapteurs];
		orientationsRelatives = new double[nbCapteurs];

		// TODO à compléter
		positionsRelatives[0] = new Vec2<ReadOnly>(70, -25);
		positionsRelatives[1] = new Vec2<ReadOnly>(70, 75);

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
		horizonCapteurs = config.getInt(ConfigInfo.HORIZON_CAPTEURS);
	}

	/**
	 * Met à jour les obstacles mobiles
	 */
	public void updateObstaclesMobiles(SensorsData data)
	{
		double orientationRobot = data.cinematique.orientation;
		Vec2<ReadOnly> positionRobot = data.cinematique.getPosition();
		boolean needNotify = false; // on ne notifie qu'une seule fois
		
		/**
		 * On prend le contrôle de gridspace. Ainsi, la mise à jour du pathfinding se fera quand tous les obstacles auront été ajoutés
		 */
		synchronized(gridspace)
		{
			/**
			 * On update la table avec notre position
			 */
		    for(GameElementNames g: GameElementNames.values)
		        if(gridspace.didWeShootIt(g, positionRobot))
		        	gridspace.setDoneTable(g, Tribool.TRUE); // on est sûr de l'avoir shooté
			
			
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
				Vec2<ReadOnly> positionVue = new Vec2<ReadOnly>(data.mesures[i], orientationsRelatives[i], true);
				if(gridspace.isObstacleFixePresentCapteurs(positionVue))
					continue;
				
				/**
				 * Sinon, on ajoute
				 */
				needNotify = true;
				Vec2<ReadWrite> positionEnnemi = new Vec2<ReadWrite>(data.mesures[i]+rayonEnnemi, orientationsRelatives[i], true);
				Vec2.plus(positionEnnemi, positionsRelatives[i]);
				Vec2.rotate(positionEnnemi, orientationRobot);
				Vec2.plus(positionEnnemi, positionRobot);
				
				if(positionEnnemi.x > 1500 || positionEnnemi.x < -1500 || positionEnnemi.y > 2000 || positionEnnemi.y < 0)
					continue; // hors table
				
				ObstacleProximity o = gridspace.addObstacleAndRemoveNearbyObstacles(positionEnnemi.getReadOnly());
				
				/**
				 * Mise à jour de l'état de la table
				 */
			    for(GameElementNames g: GameElementNames.values)
			        if(gridspace.isDoneTable(g) == Tribool.FALSE && gridspace.didTheEnemyTakeIt(g, o))
			        	gridspace.setDoneTable(g, Tribool.MAYBE);

			}
			if(needNotify)
				notify();
		}
	}
	
}
