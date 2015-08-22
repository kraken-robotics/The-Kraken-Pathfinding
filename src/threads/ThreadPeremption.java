package threads;

import obstacles.ObstaclesMemory;
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

public class ThreadPeremption extends Thread implements Service
{

	private ObstaclesMemory memory;
	protected Log log;

	private int dureePeremption;

	public ThreadPeremption(Log log, ObstaclesMemory memory)
	{
		this.log = log;
		this.memory = memory;
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			memory.deleteOldObstacles();

			long prochain = memory.getNextDeathDate();
			
			/**
			 * S'il n'y a pas d'obstacles, on dort de dureePeremption, qui est la durée minimale avant la prochaine péremption.
			 */
			if(prochain == Long.MAX_VALUE)
				Sleep.sleep(dureePeremption);
			else
				// Il faut toujours s'assurer qu'on dorme un temps positif. Il y a aussi une petite marge
				Sleep.sleep(Math.min(dureePeremption, Math.max(prochain - System.currentTimeMillis() + 5, 0)));
		}
//		log.debug("Fermeture de ThreadPeremption");
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
