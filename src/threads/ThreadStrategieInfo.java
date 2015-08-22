package threads;

import obstacles.ObstaclesIterator;
import table.StrategieInfo;
import utils.Config;
import utils.Log;
import container.Service;

/**
 * Thread d'informations stratégiques. Surveille ObstacleManager
 * TODO : à virer ?
 * @author pf
 *
 */

public class ThreadStrategieInfo extends Thread implements Service
{
	private ObstaclesIterator obstaclemanager;
	protected StrategieInfo obstaclestrategiemanager;
	protected Log log;
	
	public ThreadStrategieInfo(Log log, StrategieInfo obstaclestrategiemanager, ObstaclesIterator obstaclemanager)
	{
		this.log = log;
		this.obstaclestrategiemanager = obstaclestrategiemanager;
		this.obstaclemanager = obstaclemanager;
	}
	
	@Override
	public void run()
	{
		while(true)
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
//		log.debug("Fermeture de ThreadStrategieInfo");
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
