package tests;

import org.junit.Before;
import org.junit.After;

import table.ObstacleManager;
import utils.Log;
import utils.Config;
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
	private ObstacleManager obstaclemanager;
	
	@Before
	public void setUp() throws Exception
	{
		container = new Container();
		config = (Config) container.getService(ServiceNames.CONFIG);
		log = (Log) container.getService(ServiceNames.LOG);
		config.setDateDebutMatch();
        obstaclemanager = (ObstacleManager) container.getService(ServiceNames.OBSTACLE_MANAGER);
        // afin d'assurer l'indépendance entre tests
        obstaclemanager.clear_obstacles_mobiles();
	}

	@After
	public void tearDown() throws Exception {
		container.destructor();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	
}
