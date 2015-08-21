package threads;

import obstacles.ObstaclesMobilesMemory;
import pathfinding.GridSpace;
import container.Service;
import utils.Config;
import utils.Log;

/**
 * S'occupe de la mise à jour du cache. Surveille obstaclemanager
 * @author pf
 *
 */

public class ThreadGridSpace extends Thread implements Service {

	protected Log log;
	private ObstaclesMobilesMemory memory;
	private GridSpace gridspace;
	
	public ThreadGridSpace(Log log, ObstaclesMobilesMemory memory, GridSpace gridspace)
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
			synchronized(memory)
			{
				try {
					memory.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			gridspace.updateObstaclesMobiles();
		}
//		log.debug("Fermeture de ThreadGridSpace");

	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
