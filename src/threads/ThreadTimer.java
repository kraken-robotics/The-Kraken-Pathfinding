package threads;

import table.ObstacleManager;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Sleep;
import container.Service;

/**
 * Thread appelé périodiquement. Gère la péremption des obstacles ainsi que
 * la date de démarrage
 * @author pf
 *
 */

public class ThreadTimer extends Thread implements Service
{

	private ObstacleManager obstaclemanager;
	protected Config config;
	protected Log log;

	private long dureeMatch = 90000;
	private long dateFin;
	private int obstacleRefreshInterval = 500;

	public ThreadTimer(Log log, Config config, ObstacleManager obstaclemanager)
	{
		this.log = log;
		this.config = config;
		this.obstaclemanager = obstaclemanager;
		updateConfig();
	}
	
	@Override
	public void run()
	{
		StartMatchLock lock = StartMatchLock.getInstance();
		synchronized(lock)
		{
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// Démarrage du match!
		
		Config.matchDemarre = true;
		config.setDateDebutMatch();
		Config.capteursOn = true;
		log.debug("LE MATCH COMMENCE !");					
		dateFin = dureeMatch + Config.getDateDebutMatch();

		while(System.currentTimeMillis() < dateFin)
		{
			obstaclemanager.supprimerObstaclesPerimes();
			Sleep.sleep(obstacleRefreshInterval);
		}

		log.debug("Fin du Match !");
	}

	@Override
	public void updateConfig()
	{
		// facteur 1000 car temps_match est en secondes et duree_match en ms
		dureeMatch = 1000*config.getInt(ConfigInfo.DUREE_MATCH_EN_S);
		obstacleRefreshInterval = config.getInt(ConfigInfo.OBSTACLE_REFRESH_INTERVAL);
	}
	
}
