package tests;

import org.junit.Before;
import org.junit.After;

import utils.ConfigInfo;
import utils.Log;
import utils.Config;
import utils.Sleep;
import container.Container;
import container.ServiceNames;

/**
 * Classe mère de tous les tests.
 * Prépare container, log et config. Détruit le tout à la fin.
 * @author pf
 *
 */

public abstract class JUnit_Test
{

	protected Container container;
	protected Config config;
	protected Log log;
	
	@Before
	public void setUp() throws Exception
	{
		container = new Container();
		config = (Config) container.getService(ServiceNames.CONFIG);
		log = (Log) container.getService(ServiceNames.LOG);
		synchronized(config)
		{
			config.set(ConfigInfo.MATCH_DEMARRE, true);
			config.set(ConfigInfo.DATE_DEBUT_MATCH, System.currentTimeMillis());
		}
	}

	@After
	public void tearDown() throws Exception {
		Sleep.sleep(1000);
		container.destructor();
		Sleep.sleep(500);
	}

	
}
