package threads;

import planification.Pathfinding;
import table.ObstacleManager;
import utils.Config;
import utils.Log;

/**
 * Thread qui recalcule l'itinéraire à emprunter
 * @author pf
 *
 */

public class ThreadPathfinding extends Thread
{
	protected Log log;
	protected Config config;
	private ObstacleManager obstaclemanager;
	private Pathfinding pathfinding;
	
	public ThreadPathfinding(Log log, Config config, ObstacleManager obstaclemanager, Pathfinding pathfinding)
	{
		this.log = log;
		this.config = config;
		this.obstaclemanager = obstaclemanager;
		this.pathfinding = pathfinding;
	}

	@Override
	public void run()
	{
		while(!Config.stopThreads)
		{
			synchronized(obstaclemanager)
			{
				try {
					obstaclemanager.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			// Cet appel peut lancer un pathfinding.notifyAll()
			// Il n'est pas synchronized car il ne modifie pas obstaclemanager
			pathfinding.updateCost();
		}

	}

}
