package threads;

import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Sleep;
import container.Service;

/**
 * Un thread passionnant qui dort pendant 90s
 * @author pf
 *
 */

public class ThreadFinMatch extends ThreadAvecStop implements Service {

	protected Log log;
	protected Config config;
	private volatile Boolean matchDemarre;
	private int dureeMatch;

	public ThreadFinMatch(Log log, Config config)
	{
		this.log = log;
		this.config = config;
	}

	@Override
	public void run()
	{
		synchronized(matchDemarre)
		{
			while(!matchDemarre)
			{
				try {
					matchDemarre.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		log.debug("DEBUT DU MATCH!");
		Sleep.sleep(dureeMatch);
		config.set(ConfigInfo.FIN_MATCH, true);
		config.updateConfigServices();
		log.debug("Fin du Match !");
	}

	@Override
	public void updateConfig(Config config)
	{
		synchronized(matchDemarre)
		{
			matchDemarre = config.getBoolean(ConfigInfo.MATCH_DEMARRE);
			matchDemarre.notifyAll();
		}
	}

	@Override
	public void useConfig(Config config)
	{
		dureeMatch = config.getInt(ConfigInfo.DUREE_MATCH_EN_S)*1000;
	}

}