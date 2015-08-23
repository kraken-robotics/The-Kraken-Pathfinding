package threads;

import obstacles.Capteurs;
import pathfinding.ThetaStar;
import robot.RobotReal;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import buffer.DataForSerialOutput;
import buffer.IncomingData;
import buffer.IncomingDataBuffer;
import container.Service;


/**
 * Thread qui gère l'évitement lors d'une trajectoire courbe
 * @author pf
 *
 */

public class ThreadEvitement extends Thread implements Service
{
	private ThetaStar pathfinding;
	private DataForSerialOutput serie;
	private ThreadPathfinding threadpathfinding;
	protected Log log;
	
	private int msMaxAvantEvitement;
	
	public ThreadEvitement(Log log, ThreadPathfinding threadpathfinding, DataForSerialOutput serie, ThetaStar pathfinding)
	{
		this.log = log;
		this.pathfinding = pathfinding;
		this.serie = serie;
		this.threadpathfinding = threadpathfinding;
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			synchronized(threadpathfinding)
			{
				try {
					threadpathfinding.wait();
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
				synchronized(pathfinding)
				{
					if(threadpathfinding.isUrgence())
						pathfinding.wait(msMaxAvantEvitement);
					else
						pathfinding.wait(100);
				}
			}
			
		}
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		msMaxAvantEvitement = config.getInt(ConfigInfo.MS_MAX_AVANT_EVITEMENT);
	}

}