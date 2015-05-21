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
	protected Log log;
	private Config config;
	private IncomingDataBuffer buffer;
	private StartMatchLock lock;

	private long dureeMatch = 90000;
	private long dateFin;
	private int obstacleRefreshInterval = 500;

	public ThreadTimer(Log log, Config config, ObstacleManager obstaclemanager, IncomingDataBuffer buffer, StartMatchLock lock)
	{
		this.log = log;
		this.config = config;
		this.obstaclemanager = obstaclemanager;
		this.buffer = buffer;
		this.lock = lock;
	}
	
	@Override
	public void run()
	{
		synchronized(lock)
		{
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// Démarrage du match!
		
		config.set(ConfigInfo.MATCH_DEMARRE, true);
		config.set(ConfigInfo.DATE_DEBUT_MATCH, System.currentTimeMillis());
		config.set(ConfigInfo.CAPTEURS_ON, true);
		log.debug("LE MATCH COMMENCE !");					
		dateFin = dureeMatch + config.getInt(ConfigInfo.DATE_DEBUT_MATCH);

		while(System.currentTimeMillis() < dateFin)
		{
			obstaclemanager.supprimerObstaclesPerimes();
			buffer.notifyIfNecessary();
			Sleep.sleep(obstacleRefreshInterval);
		}

		log.debug("Fin du Match !");
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		// facteur 1000 car temps_match est en secondes et duree_match en ms
		dureeMatch = 1000*config.getInt(ConfigInfo.DUREE_MATCH_EN_S);
		obstacleRefreshInterval = config.getInt(ConfigInfo.OBSTACLE_REFRESH_INTERVAL);
	}

}
