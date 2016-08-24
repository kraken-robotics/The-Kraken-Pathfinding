package threads;

import serie.SerialLowLevel;
import utils.Config;
import utils.ConfigInfo;
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
	private int sleep;
	
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
			Sleep.sleep(time+sleep);
			serie.retry();
		}
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		sleep = config.getInt(ConfigInfo.SLEEP_ENTRE_TRAMES);
	}

}
