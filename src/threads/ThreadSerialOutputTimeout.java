package threads;

import serie.SerialLowLevel;
import utils.Config;
import utils.Log;
import utils.Sleep;
import container.Service;

/**
 * Thread qui permet de faire gaffe au timeout de la série bas niveau
 * @author pf
 *
 */

public class ThreadSerialOutputTimeout extends Thread implements Service
{
	protected Log log;
	private SerialLowLevel serie;
	
	public ThreadSerialOutputTimeout(Log log, SerialLowLevel serie)
	{
		this.log = log;
		this.serie = serie;
	}

	@Override
	public void run()
	{
		while(true)
		{
			int time = serie.timeBeforeRetry();
			Sleep.sleep(time);
			serie.retry();
		}
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
