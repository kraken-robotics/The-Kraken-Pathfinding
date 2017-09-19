/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;

import pfg.config.Config;
import pfg.graphic.WindowFrame;
import pfg.injector.Injector;
import pfg.kraken.Kraken;
import pfg.kraken.LogCategoryKraken;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.utils.XY;
import pfg.log.Log;

/**
 * Classe mère de tous les tests.
 * Prépare container, log et config. Crée l'interface graphique si besoin est.
 * Détruit le tout à la fin.
 * 
 * @author pf
 *
 */

public abstract class JUnit_Test
{
	protected Config config;
	protected Log log;
	protected Injector injector;
	protected Kraken kraken;
	protected WindowFrame f;

	@Rule
	public TestName testName = new TestName();

	@Before
	public void setUp() throws Exception
	{
		System.out.println("----- DÉBUT DU TEST " + testName.getMethodName() + " -----");

		kraken = Kraken.getKraken(null, new XY(-1500, 0), new XY(1500, 2000), Arrays.asList("test")); // TODO
		
		Method m = Kraken.class.getDeclaredMethod("getInjector");
		m.setAccessible(true);
		injector = (Injector) m.invoke(kraken);
		config = injector.getService(Config.class);
		log = injector.getService(Log.class);
		f = injector.getExistingService(WindowFrame.class);
		log.write("Test unitaire : " + testName.getMethodName(), LogCategoryKraken.TEST);
	}
	
	public void setUpWith(List<Obstacle> fixedObstacles) throws Exception
	{
		System.out.println("----- DÉBUT DU TEST " + testName.getMethodName() + " -----");

		kraken = Kraken.getKraken(fixedObstacles, new XY(-1500, 0), new XY(1500, 2000), Arrays.asList("test"));
		Method m = Kraken.class.getDeclaredMethod("getInjector");
		m.setAccessible(true);
		injector = (Injector) m.invoke(kraken);
		config = injector.getService(Config.class);
		log = injector.getService(Log.class);
		f = injector.getExistingService(WindowFrame.class);
		log.write("Test unitaire : " + testName.getMethodName(), LogCategoryKraken.TEST);
	}

	@After
	public void tearDown() throws Exception
	{
		if(f != null)
			f.waitUntilExit(5000);
		kraken.destructor();
	}

	/**
	 * Lanceur d'une seule méthode de test
	 * 
	 * @param args
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws ClassNotFoundException
	{
		String[] classAndMethod = args[0].split("#");
		Request request = Request.method(Class.forName(classAndMethod[0]), classAndMethod[1]);

		Result result = new JUnitCore().run(request);
		System.exit(result.wasSuccessful() ? 0 : 1);
	}

}
