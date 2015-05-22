package threads;

import table.ObstacleManager;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Sleep;
import container.Service;

/**
 * Thread qui gère la péremption des obstacles en dormant
 * le temps exact entre deux péremptions.
 * @author pf
 *
 */

public class ThreadPeremption extends ThreadAvecStop implements Service
{

	private ObstacleManager obstaclemanager;
	protected Log log;

	private int dureePeremption;

	public ThreadPeremption(Log log, ObstacleManager obstaclemanager)
	{
		this.log = log;
		this.obstaclemanager = obstaclemanager;
	}
	
	@Override
	public void run()
	{
		while(!finThread)
		{
			obstaclemanager.supprimerObstaclesPerimes();
			long prochain = obstaclemanager.getDateSomethingChange();
			
			/**
			 * S'il n'y a pas d'obstacles, on dort de dureePeremption, qui est la durée minimale avant la prochaine péremption.
			 */
			if(prochain == Long.MAX_VALUE)
				Sleep.sleep(dureePeremption);
			else
				// Il faut toujours s'assurer qu'on dorme un temps positif.
				Sleep.sleep(Math.min(dureePeremption, Math.max(prochain - System.currentTimeMillis(), 0)));
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
