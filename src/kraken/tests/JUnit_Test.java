/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package kraken.tests;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.After;
import graphic.Fenetre;
import kraken.container.Container;
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
	protected Container container;
	protected Config config;
	protected Log log;

	@Rule
	public TestName testName = new TestName();

	@Before
	public void setUp() throws Exception
	{
		System.out.println("----- DÉBUT DU TEST " + testName.getMethodName() + " -----");

		container = new Container(null);
		config = container.getService(Config.class);
		log = container.getService(Log.class);
		log.debug("Test unitaire : " + testName.getMethodName());
	}

	@After
	public void tearDown() throws Exception
	{
		Fenetre f = container.getExistingService(Fenetre.class);
		if(f != null)
			f.waitUntilExit();
		container.destructor();
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
