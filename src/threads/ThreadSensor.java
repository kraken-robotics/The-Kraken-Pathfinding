package threads;

import permissions.ReadOnly;
import permissions.ReadWrite;
import container.Service;
import exceptions.FinMatchException;
import robot.RobotReal;
import robot.stm.STMcard;
import strategie.GameState;
import table.ObstacleManager;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Sleep;
import utils.Vec2;

/**
 * Thread qui ajoute en continu les obstacles détectés par les capteurs
 * @author pf
 *
 */

public class ThreadSensor extends RobotThread implements Service
{
	private Log log;
	private Config config;
	private STMcard stm;
	private final GameState<RobotReal,ReadWrite> state;
	private ObstacleManager obstaclemanager;
	
	private double tempo = 0;
	private int nbCapteurs;
	private int table_x = 3000;
	private int table_y = 2000;
	private int diametreEnnemi;
	
	public ThreadSensor(Log log, Config config, GameState<RobotReal,ReadWrite> state, ObstacleManager obstaclemanager, STMcard stm)
	{
		this.log = log;
		this.config = config;
		this.stm = stm;
		this.state = state;
		this.obstaclemanager = obstaclemanager;
		
		Thread.currentThread().setPriority(2);
		updateConfig();
	}
	
	@Override
	public void run()
	{
		log.debug("Lancement du thread de capteurs");
		int date_dernier_ajout = 0;
		
		while(!Config.matchDemarre)
		{
			if(stopThreads)
			{
				log.debug("Stoppage du thread capteurs");
				return;
			}
			Sleep.sleep(50);
		}
		
		log.debug("Activation des capteurs");
		while(!finMatch)
		{
			try {
				if(stopThreads)
				{
					log.debug("Stoppage du thread capteurs");
					return;
				}

				Vec2<ReadOnly>[] positions = stm.mesurer();
	
				for(int i = 0; i < nbCapteurs; i++)
				{
					// On ne prend pas en compte le rayon du robot adverse dans la position brute. Il s'agit en fait du point effectivement vu
					// Utilisé pour voir si l'obstacle n'est justement pas un robot adverse.
					Vec2<ReadOnly> positionBrute = positions[2*i];
					Vec2<ReadOnly> position = positions[2*i+1];
	
					// si la position est bien sur la table (histoire de pas détecter un arbitre)
					// on vérifie qu'un obstacle n'a pas été ajouté récemment
					if(System.currentTimeMillis() - date_dernier_ajout > tempo &&
							position.x - diametreEnnemi > -table_x / 2 && position.y > diametreEnnemi && position.x + diametreEnnemi < table_x / 2 && position.y + diametreEnnemi < table_y)
						if(!obstaclemanager.isObstacleFixePresentCapteurs(positionBrute))
						{
							GameState.creer_obstacle(state, position);
							date_dernier_ajout = (int)System.currentTimeMillis();
							log.debug("Nouvel obstacle en "+position);
						}
						else
						    log.debug("L'objet vu en "+positionBrute+" est un obstacle fixe.");
				}
			}
			catch(FinMatchException e)
			{
				break;
			}
		}
        log.debug("Fin du thread de capteurs");
	}
	
	public void updateConfig()
	{
		nbCapteurs = config.getInt(ConfigInfo.NB_CAPTEURS_PROXIMITE);
		tempo = config.getDouble(ConfigInfo.CAPTEURS_TEMPORISATION_OBSTACLES);
		table_x = config.getInt(ConfigInfo.TABLE_X);
		table_y = config.getInt(ConfigInfo.TABLE_Y);
		diametreEnnemi = 2*config.getInt(ConfigInfo.RAYON_ROBOT_ADVERSE);
		log.updateConfig();
		stm.updateConfig();
		state.updateConfig();
		obstaclemanager.updateConfig();;
	}

}
