package threads;

import container.Service;
import planification.LocomotionArc;
import planification.Chemin;
import planification.Pathfinding;
import strategie.Strategie;
import strategie.StrategieArc;
import table.StrategieInfo;
import utils.Config;
import utils.Log;

/**
 * Thread qui recalcule la stratégie à suivre
 * @author pf
 *
 */

public class ThreadStrategie extends Thread implements Service
{
	protected Log log;
	protected Config config;
	private StrategieInfo obstaclestrategiemanager;
	private Strategie strategie;
	
	public ThreadStrategie(Log log, Config config, StrategieInfo obstaclestrategiemanager, Strategie strategie)
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

	@Override
	public void updateConfig()
	{}

}
