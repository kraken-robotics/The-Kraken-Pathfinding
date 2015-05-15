package threads;

import table.ObstacleManager;
import table.Table;
import threads.IncomingDataBuffer.Elem;
import utils.Config;
import utils.Log;
import container.Service;

/**
 * Thread du manager d'obstacle. Surveille IncomingDataBuffer
 * @author pf
 *
 */

public class ThreadObstacleManager extends Thread implements Service
{
	private IncomingDataBuffer buffer;
	private ObstacleManager obstaclemanager;
	protected Config config;
	protected Log log;
	
	public ThreadObstacleManager(Log log, Config config, IncomingDataBuffer buffer, ObstacleManager obstaclemanager)
	{
		this.log = log;
		this.config = config;
		this.buffer = buffer;
		this.obstaclemanager = obstaclemanager;
		updateConfig();
	}
	
	@Override
	public void run()
	{
		while(!Config.stopThreads)
		{
			Elem e;
			synchronized(buffer)
			{
				try {
					buffer.wait();
					e = buffer.poll();					
				} catch (InterruptedException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
			}
			
			// Cet appel peut lancer un obstaclemanager.notifyAll()
			// Il n'est pas synchronized car il ne modifie pas le buffer
			obstaclemanager.addIfUseful(e);
		}
	}
	
	@Override
	public void updateConfig()
	{
		buffer.updateConfig();
		obstaclemanager.updateConfig();
	}

	
	
}
