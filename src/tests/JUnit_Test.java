package tests;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.After;

import utils.ConfigInfo;
import utils.Log;
import utils.Config;
import container.Container;
import debug.Fenetre;
import threads.ThreadShutdown;

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
	protected Fenetre fenetre;
	
    @Rule public TestName testName = new TestName();
    
	@Before
	public void setUp() throws Exception
	{
		System.err.println("----- DÉBUT DU TEST "+testName.getMethodName()+" -----\n\n");

		container = new Container();
		config = container.getService(Config.class);
		log = container.getService(Log.class);
		fenetre = container.getService(Fenetre.class);
		synchronized(config)
		{
			config.set(ConfigInfo.MATCH_DEMARRE, true);
			config.set(ConfigInfo.DATE_DEBUT_MATCH, System.currentTimeMillis());
		}
	}

	@After
	public void tearDown() throws Exception {
		if(!fenetre.needInit)
			Thread.sleep(100000);
		Runtime.getRuntime().removeShutdownHook(ThreadShutdown.getInstance());
		container.destructor(true);
		System.out.println("\n\n");
	}

	
}
