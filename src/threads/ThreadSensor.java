package threads;

import container.Service;
import exceptions.FinMatchException;
import robot.cardsWrappers.SensorsCardWrapper;
import table.GridSpace;
import table.ObstacleManager;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Sleep;
import utils.Vec2;

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
	private GridSpace gridspace;
	private ObstacleManager obstaclemanager;
	
	private double tempo = 0;
	private int nbCapteurs;
	private int table_x = 3000;
	private int table_y = 2000;
	
	public ThreadSensor(Log log, Config config, GridSpace gridspace, ObstacleManager obstaclemanager, SensorsCardWrapper capteur)
	{
		this.log = log;
		this.config = config;
		this.capteur = capteur;
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

				Vec2[] positions = capteur.mesurer();

//				if(distance >= 40 && distance < horizon_capteurs)
//				{
//					int distance_inter_robots = distance + rayon_robot_adverse + largeur_robot/2;
//					int distance_brute = distance + largeur_robot/2;
//					double theta = robotreal.getOrientation();
	
				for(int i = 0; i < nbCapteurs; i++)
				{
					// On ne prend pas en compte le rayon du robot adverse dans la position brute. Il s'agit en fait du point effectivement vu
					// Utilisé pour voir si l'obstacle n'est justement pas un robot adverse.
					Vec2 positionBrute = positions[2*i];
					Vec2 position = positions[2*i+1];
	
					// si la position est bien sur la table (histoire de pas détecter un arbitre)
					// on vérifie qu'un obstacle n'a pas été ajouté récemment
					// TODO: proprifier la valeur hardcodée 200
					if(System.currentTimeMillis() - date_dernier_ajout > tempo &&
							position.x-200 > -table_x/2 && position.y > 200 && position.x+200 < table_x/2 && position.y+200 < table_y)
						if(!obstaclemanager.isObstacleFixePresentCapteurs(positionBrute))
						{
							gridspace.creer_obstacle(position);
							date_dernier_ajout = (int)System.currentTimeMillis();
							log.debug("Nouvel obstacle en "+position, this);
						}
						else
						    log.debug("L'objet vu en "+positionBrute+" est un obstacle fixe.", this);
				}
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
			nbCapteurs = config.getInt(ConfigInfo.NB_CAPTEURS_PROXIMITE);
			tempo = config.getDouble(ConfigInfo.CAPTEURS_TEMPORISATION_OBSTACLES);
			table_x = config.getInt(ConfigInfo.TABLE_X);
			table_y = config.getInt(ConfigInfo.TABLE_Y);
	}

}
