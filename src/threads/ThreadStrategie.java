package threads;

import container.Service;
import planification.LocomotionArc;
import planification.Path;
import planification.Pathfinding;
import planification.StrategieArc;
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
	private Path<StrategieArc> strategie;
	private Pathfinding<LocomotionArc> pathfinding;
	
	public ThreadStrategie(Log log, Config config, StrategieInfo obstaclestrategiemanager, Path<StrategieArc> strategie, Pathfinding<LocomotionArc> pathfinding)
	{
		this.log = log;
		this.config = config;
		this.obstaclestrategiemanager = obstaclestrategiemanager;
		this.strategie = strategie;
		this.pathfinding = pathfinding;
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
