package threads;

import table.ObstacleManager;
import utils.Config;
import utils.Log;
import utils.Sleep;
import container.Service;

/**
 * Thread appelé périodiquement. Gère la péremption des obstacles
 * @author pf
 *
 */

public class ThreadTimer extends Thread implements Service
{

	private ObstacleManager obstaclemanager;
	protected Config config;
	protected Log log;
	
	public ThreadTimer(Log log, Config config, IncomingDataBuffer buffer, ObstacleManager obstaclemanager)
	{
		this.log = log;
		this.config = config;
		this.obstaclemanager = obstaclemanager;
		updateConfig();
	}
	
	@Override
	public void run()
	{
		// demande à obstaclemanager de faire le ménage
		Sleep.sleep(5000); // TODO config
	}

	@Override
	public void updateConfig() {
		// TODO Auto-generated method stub
		
	}
	
}
