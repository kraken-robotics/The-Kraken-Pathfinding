package threads;

import table.GridSpace;
import table.ObstacleManager;
import table.ObstaclesMobilesIterator;
import table.ObstaclesMobilesMemory;
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

	private ObstaclesMobilesMemory memory;
	protected Log log;
	private GridSpace gridspace;

	private int dureePeremption;

	public ThreadPeremption(Log log, ObstaclesMobilesMemory memory, GridSpace gridspace)
	{
		this.log = log;
		this.memory = memory;
		this.gridspace = gridspace;
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			// petite marge
			if(memory.update())
				gridspace.update();
			long prochain = memory.getNextDeathDate() + 5;
			
			/**
			 * S'il n'y a pas d'obstacles, on dort de dureePeremption, qui est la durée minimale avant la prochaine péremption.
			 */
			if(prochain == Long.MAX_VALUE)
				Sleep.sleep(dureePeremption);
			else
				// Il faut toujours s'assurer qu'on dorme un temps positif.
				Sleep.sleep(Math.min(dureePeremption, Math.max(prochain - System.currentTimeMillis(), 0)));
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
