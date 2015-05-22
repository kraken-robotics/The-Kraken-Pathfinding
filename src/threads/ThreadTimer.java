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
	private volatile Boolean matchDemarre;

	private long dureeMatch = 90000;
	private long dateFin;
	private int dureePeremption;

	public ThreadTimer(Log log, Config config, ObstacleManager obstaclemanager, IncomingDataBuffer buffer)
	{
		this.log = log;
		this.config = config;
		this.obstaclemanager = obstaclemanager;
		this.buffer = buffer;
	}
	
	@Override
	public void run()
	{
		synchronized(matchDemarre)
		{
			try {
				matchDemarre.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		log.debug("LE MATCH COMMENCE !");					

		while(System.currentTimeMillis() < dateFin)
		{
			obstaclemanager.supprimerObstaclesPerimes();
			buffer.notifyIfNecessary();
			// TODO: faire un sleep exact
			int prochain = obstaclemanager.getDateSomethingChange();
			
			/**
			 * S'il n'y a pas d'obstacles, on dort de dureePeremption, qui est la durée minimale avant la prochaine péremption.
			 */
			if(prochain == Integer.MAX_VALUE)
				Sleep.sleep(dureePeremption);
			else
				Sleep.sleep(prochain - System.currentTimeMillis());
		}
		config.set(ConfigInfo.FIN_MATCH, true);
		config.updateConfigServices();
		log.debug("Fin du Match !");
	}

	@Override
	public void updateConfig(Config config)
	{
		synchronized(matchDemarre)
		{
			dateFin = dureeMatch + config.getInt(ConfigInfo.DATE_DEBUT_MATCH);
			matchDemarre = config.getBoolean(ConfigInfo.MATCH_DEMARRE);
			matchDemarre.notifyAll();
		}
	}

	@Override
	public void useConfig(Config config)
	{
		// facteur 1000 car temps_match est en secondes et duree_match en ms
		dureeMatch = 1000*config.getInt(ConfigInfo.DUREE_MATCH_EN_S);
		dureePeremption = 1000*config.getInt(ConfigInfo.DUREE_PEREMPTION_OBSTACLES);
//		obstacleRefreshInterval = config.getInt(ConfigInfo.OBSTACLE_REFRESH_INTERVAL);
	}

}
