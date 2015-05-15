package threads;

import planification.Pathfinding;
import table.StrategieInfo;
import utils.Config;
import utils.Log;

/**
 * Thread qui recalcule la stratégie à suivre
 * @author pf
 *
 */

public class ThreadStrategie extends Thread
{
	protected Log log;
	protected Config config;
	private StrategieInfo obstaclestrategiemanager;
	private Pathfinding strategie;
	
	public ThreadStrategie(Log log, Config config, StrategieInfo obstaclestrategiemanager, Pathfinding strategie)
	{
		this.log = log;
		this.config = config;
		this.obstaclestrategiemanager = obstaclestrategiemanager;
		this.strategie = strategie;
	}

	@Override
	public void run()
	{
		while(!Config.stopThreads)
		{
			synchronized(obstaclestrategiemanager)
			{
				try {
					obstaclestrategiemanager.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			// Cet appel peut lancer un pathfinding.notifyAll()
			// Il n'est pas synchronized car il ne modifie pas obstaclemanager
			strategie.updateCost();
		}

	}

}
