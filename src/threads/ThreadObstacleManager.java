package threads;

import buffer.IncomingData;
import buffer.IncomingDataBuffer;
import robot.RobotReal;
import table.ObstacleManager;
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
	private RobotReal robot;
	
	protected Log log;
	
	public ThreadObstacleManager(Log log, IncomingDataBuffer buffer, ObstacleManager obstaclemanager, RobotReal robot)
	{
		this.log = log;
		this.buffer = buffer;
		this.obstaclemanager = obstaclemanager;
		this.robot = robot;
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			IncomingData e = null;
			synchronized(buffer)
			{
				try {
					while(buffer.isEmpty())
						buffer.wait(100);
//					log.debug("Réveil de ThreadObstacleManager");
					e = buffer.poll();
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
			}
			// Cet appel peut lancer un obstaclemanager.notifyAll()
			// Il n'est pas synchronized car il ne modifie pas le buffer
//			if(e != null)
			robot.setPositionOrientationJava(e.positionRobot, e.orientationRobot);
			obstaclemanager.updateObstaclesMobiles(e);
			
		}
//		log.debug("Fermeture de ThreadObstacleManager");
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
