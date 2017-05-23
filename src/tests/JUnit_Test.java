/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package tests;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.After;

import utils.Log;
import config.Config;
import config.ConfigInfo;
import container.Container;
import graphic.Fenetre;
import robot.Cinematique;
import robot.RobotReal;
import threads.serie.ThreadSerialInputCoucheOrdre;

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

		container = new Container();
		config = container.getService(Config.class);
		log = container.getService(Log.class);
		log.debug("Test unitaire : " + testName.getMethodName());
		synchronized(config)
		{
			config.set(ConfigInfo.MATCH_DEMARRE, true);
			config.set(ConfigInfo.DATE_DEBUT_MATCH, System.currentTimeMillis());
			ThreadSerialInputCoucheOrdre.capteursOn = true;
		}
		/*
		 * La position initiale du robot
		 */
		RobotReal r = container.getService(RobotReal.class);
		r.setCinematique(new Cinematique(0, 1800, -Math.PI / 3, true, 0));
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
