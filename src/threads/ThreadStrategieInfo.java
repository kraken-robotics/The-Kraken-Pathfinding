package threads;

import table.ObstacleManager;
import table.StrategieInfo;
import utils.Config;
import utils.Log;
import container.Service;

/**
 * Thread de table. Surveille ObstacleManager
 * @author pf
 *
 */

public class ThreadStrategieInfo extends Thread implements Service
{
	private ObstacleManager obstaclemanager;
	private StrategieInfo obstaclestrategiemanager;
	protected Config config;
	protected Log log;
	
	public ThreadStrategieInfo(Log log, Config config, StrategieInfo obstaclestrategiemanager, ObstacleManager obstaclemanager)
	{
		this.log = log;
		this.config = config;
		this.obstaclestrategiemanager = obstaclestrategiemanager;
		this.obstaclemanager = obstaclemanager;
		
		updateConfig();
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
				} catch (InterruptedException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
			}
			
			// Cet appel peut lancer un obstaclestrategiemanager.notifyAll()
			// Il n'est pas synchronized car il ne modifie pas le buffer
			// TODO
		}
	}
	
	@Override
	public void updateConfig()
	{
		obstaclestrategiemanager.updateConfig();
		obstaclemanager.updateConfig();
	}

	
	
}
