/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import java.lang.reflect.Method;
import org.junit.After;
import graphic.Fenetre;
import injector.Injector;
import kraken.Kraken;
import kraken.utils.Log;

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

	@Rule
	public TestName testName = new TestName();

	@Before
	public void setUp() throws Exception
	{
		System.out.println("----- DÉBUT DU TEST " + testName.getMethodName() + " -----");

		kraken = new Kraken(null);
		
		Method m = Kraken.class.getDeclaredMethod("getInjector");
		m.setAccessible(true);
		injector = (Injector) m.invoke(kraken);
		config = injector.getService(Config.class);
		log = injector.getService(Log.class);
		log.debug("Test unitaire : " + testName.getMethodName());
	}

	@After
	public void tearDown() throws Exception
	{
		Fenetre f = injector.getExistingService(Fenetre.class);
		if(f != null)
			f.waitUntilExit();
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
