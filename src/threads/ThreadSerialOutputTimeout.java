package threads;

import serie.SerieCoucheTrame;
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
	private SerieCoucheTrame serie;
	private int sleep;
	
	public ThreadSerialOutputTimeout(Log log, SerieCoucheTrame serie)
	{
		this.log = log;
		this.serie = serie;
	}

	@Override
	public void run()
	{
		while(true)
		{
			int timeResend = serie.timeBeforeResend();
			int timeDeath = serie.timeBeforeDeath();
			
			if(timeDeath <= timeResend)
			{
				Sleep.sleep(timeDeath+sleep);
				serie.kill();
			}
			else
			{
				Sleep.sleep(timeResend+sleep);
				serie.resend();
			}			
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
