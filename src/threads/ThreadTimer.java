package threads;

import buffer.IncomingDataBuffer;
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

public class ThreadTimer extends ThreadAvecStop implements Service
{

	private ObstacleManager obstaclemanager;
	protected Log log;
	private IncomingDataBuffer buffer;

	private int dureePeremption;

	public ThreadTimer(Log log, ObstacleManager obstaclemanager, IncomingDataBuffer buffer)
	{
		this.log = log;
		this.obstaclemanager = obstaclemanager;
		this.buffer = buffer;
	}
	
	@Override
	public void run()
	{
		while(!finThread)
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
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		dureePeremption = config.getInt(ConfigInfo.DUREE_PEREMPTION_OBSTACLES);
	}

}
