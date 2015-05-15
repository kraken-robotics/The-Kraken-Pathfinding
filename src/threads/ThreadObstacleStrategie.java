package threads;

import table.ObstacleManager;
import table.ObstacleStrategieManager;
import table.Table;
import threads.IncomingDataBuffer.Elem;
import utils.Config;
import utils.Log;
import container.Service;

/**
 * Thread de table. Surveille IncomingDataBuffer
 * @author pf
 *
 */

public class ThreadObstacleStrategie extends Thread implements Service
{
	private ObstacleManager obstaclemanager;
	private ObstacleStrategieManager obstaclestrategiemanager;
	protected Config config;
	protected Log log;
	
	public ThreadObstacleStrategie(Log log, Config config, ObstacleStrategieManager obstaclestrategiemanager, ObstacleManager obstaclemanager)
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
			obstaclestrategiemanager.addIfUseful(e);
		}
	}
	
	@Override
	public void updateConfig()
	{
		obstaclestrategiemanager.updateConfig();
		obstaclemanager.updateConfig();
	}

	
	
}
