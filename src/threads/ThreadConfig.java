package threads;

import container.Container;
import container.Service;
import container.ServiceNames;
import utils.Config;
import utils.Log;

/**
 * S'occupe de la mise a jour de la config. Surveille config
 * @author pf
 *
 */

public class ThreadConfig extends Thread implements Service {

	protected Log log;
	protected Config config;
	private Container container;
	
	public ThreadConfig(Log log, Config config, Container container)
	{
		this.log = log;
		this.container = container;
		this.config = config;
	}

	@Override
	public void run()
	{
		while(true)
		{
 //			if(Config.debugMutex)
//				log.debug("Tentative de récupération du mutex de config");
			synchronized(config)
			{
//				if(Config.debugMutex)
//					log.debug("Mutex de config récupéré");
				try {
					config.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
//			if(Config.debugMutex)
//				log.debug("Mutex de config libéré");

			log.debug("Réveil de ThreadConfig");	
			
			for(ServiceNames name: ServiceNames.values())
			{
				Service service = container.getInstanciedService(name);
				if(service != null)
					service.updateConfig(config);
			}
		}
//		log.debug("Fermeture de ThreadConfig");
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
