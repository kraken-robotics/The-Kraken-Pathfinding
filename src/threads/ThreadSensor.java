package threads;

import container.Service;
import enums.ConfigInfo;
import exceptions.FinMatchException;
import obstacles.ObstacleManager;
import pathfinding.GridSpace;
import robot.RobotReal;
import robot.cardsWrappers.SensorsCardWrapper;
import smartMath.Vec2;
import utils.Config;
import utils.Log;
import utils.Sleep;

/**
 * Thread qui ajoute en continu les obstacles détectés par les capteurs
 * @author pf, Krissprolls
 *
 */

public class ThreadSensor extends AbstractThread implements Service
{
	private Log log;
	private Config config;
	private SensorsCardWrapper capteur;
	private RobotReal robotreal;
	private GridSpace gridspace;
	private ObstacleManager obstaclemanager;
	
	private double tempo = 0;
	private int horizon_capteurs = 700;
	private int rayon_robot_adverse = 230;
	private int largeur_robot = 300;
	private int table_x = 3000;
	private int table_y = 2000;
	private int capteurs_frequence = 5;
	
	public ThreadSensor(Log log, Config config, RobotReal robotreal, GridSpace gridspace, ObstacleManager obstaclemanager, SensorsCardWrapper capteur)
	{
		this.log = log;
		this.config = config;
		this.capteur = capteur;
		this.robotreal = robotreal;
		this.gridspace = gridspace;
		this.obstaclemanager = obstaclemanager;
		
		Thread.currentThread().setPriority(2);
		updateConfig();
	}
	
	@Override
	public void run()
	{
		log.debug("Lancement du thread de capteurs", this);
		int date_dernier_ajout = 0;
		
		while(!Config.matchDemarre)
		{
			if(stopThreads)
			{
				log.debug("Stoppage du thread capteurs", this);
				return;
			}
			Sleep.sleep(50);
		}
		
		log.debug("Activation des capteurs", this);
		while(!finMatch)
		{
			try {
				if(stopThreads)
				{
					log.debug("Stoppage du thread capteurs", this);
					return;
				}

				int distance = capteur.mesurer();
				if (distance > 0 && distance < 70)
					log.critical("Câlin !", this);
				
				if(distance >= 40 && distance < horizon_capteurs)
				{
					int distance_inter_robots = distance + rayon_robot_adverse + largeur_robot/2;
					int distance_brute = distance + largeur_robot/2;
					double theta = robotreal.getOrientation();
	
					// On ne prend pas en compte le rayon du robot adverse dans la position brute. Il s'agit en fait du point effectivement vu
					// Utilisé pour voir si l'obstacle n'est justement pas un robot adverse.
					Vec2 position_brute = robotreal.getPosition().plusNewVector(new Vec2((int)((float)distance_brute * (float)Math.cos(theta)), (int)((float)distance_brute * (float)Math.sin(theta)))); // position du point détecté
					Vec2 position = robotreal.getPosition().plusNewVector(new Vec2((int)(distance_inter_robots * Math.cos(theta)), (int)((float)distance_inter_robots * (float)Math.sin(theta)))); // centre supposé de l'obstacle détecté
	
					// si la position est bien sur la table (histoire de pas détecter un arbitre)
					if(position.x-200 > -table_x/2 && position.y > 200 && position.x+200 < table_x/2 && position.y+200 < table_y)
						// on vérifie qu'un obstacle n'a pas été ajouté récemment
						if(System.currentTimeMillis() - date_dernier_ajout > tempo)
						{
							if(!obstaclemanager.is_obstacle_fixe_present_capteurs(position_brute))
							{
								gridspace.creer_obstacle(position);
								date_dernier_ajout = (int)System.currentTimeMillis();
								log.debug("Nouvel obstacle en "+position, this);
							}
							else	
							    log.debug("L'objet vu en "+position_brute+" est un obstacle fixe.", this);
						}
	
				}
				
				Sleep.sleep((long)(1000./capteurs_frequence));
			}
			catch(FinMatchException e)
			{
				break;
			}
		}
        log.debug("Fin du thread de capteurs", this);
	}
	
	public void updateConfig()
	{
			tempo = config.getDouble(ConfigInfo.CAPTEURS_TEMPORISATION_OBSTACLES);
			horizon_capteurs = config.getInt(ConfigInfo.HORIZON_CAPTEURS);
			rayon_robot_adverse = config.getInt(ConfigInfo.RAYON_ROBOT_ADVERSE);
			largeur_robot = config.getInt(ConfigInfo.LARGEUR_ROBOT);
			table_x = config.getInt(ConfigInfo.TABLE_X);
			table_y = config.getInt(ConfigInfo.TABLE_Y);
			capteurs_frequence = config.getInt(ConfigInfo.CAPTEURS_FREQUENCE);
	}

}
