package threads;

import utils.Log;
import utils.Config;
import container.Service;

/**
 * Classe abstraite des threads
 * @author pf,marsu
 *
 */

public abstract class AbstractThread extends Thread implements Service {

	protected static Config config;
	protected static Log log;

	protected static boolean stop_threads = false;
	
	public AbstractThread(Service config, Service log)
	{
		AbstractThread.config = (Config) config;
		AbstractThread.log = (Log) log;
	}

	protected AbstractThread()
	{		
	}

	public void updateConfig()
	{
	}
	
	public abstract void run();

}

