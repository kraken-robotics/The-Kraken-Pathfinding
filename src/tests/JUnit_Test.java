package tests;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.After;

import utils.ConfigInfo;
import utils.Log;
import utils.Config;
import container.Container;
import container.ServiceNames;
import debug.Fenetre;
import threads.ThreadExit;

/**
 * Classe mère de tous les tests.
 * Prépare container, log et config. Crée l'interface graphique si besoin est. Détruit le tout à la fin.
 * @author pf
 *
 */

public abstract class JUnit_Test
{
	protected Container container;
	protected Config config;
	protected Log log;
	
    @Rule public TestName testName = new TestName();
    
	@SuppressWarnings("unused")
	@Before
	public void setUp() throws Exception
	{
		System.err.println("----- DÉBUT DU TEST "+testName.getMethodName()+" -----\n\n");

		container = new Container();
		config = (Config) container.getService(ServiceNames.CONFIG);
		log = (Log) container.getService(ServiceNames.LOG);
		synchronized(config)
		{
			config.set(ConfigInfo.MATCH_DEMARRE, true);
			config.set(ConfigInfo.DATE_DEBUT_MATCH, System.currentTimeMillis());
		}
		if(Config.graphicDStarLite || Config.graphicThetaStar || Config.graphicObstacles)
			Fenetre.setInstance(container);
	}

	@SuppressWarnings("unused")
	@After
	public void tearDown() throws Exception {
		if((Config.graphicDStarLite || Config.graphicThetaStar || Config.graphicObstacles) && !Fenetre.needInit)
			Thread.sleep(100000);
		Runtime.getRuntime().removeShutdownHook(ThreadExit.getInstance());
		container.destructor();
		System.out.println("\n\n");
	}

	
}
