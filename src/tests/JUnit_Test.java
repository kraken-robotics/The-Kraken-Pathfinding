/*
Copyright (C) 2013-2017 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package tests;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.After;

import utils.Log;
import config.Config;
import config.ConfigInfo;
import container.Container;
import graphic.Fenetre;
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
	
    @Rule public TestName testName = new TestName();
    
	@Before
	public void setUp() throws Exception
	{
		System.err.println("----- DÉBUT DU TEST "+testName.getMethodName()+" -----\n\n");

		container = new Container();
		config = container.getService(Config.class);
		log = container.getService(Log.class);
		synchronized(config)
		{
			config.set(ConfigInfo.MATCH_DEMARRE, true);
			config.set(ConfigInfo.DATE_DEBUT_MATCH, System.currentTimeMillis());
		}
	}

	@After
	public void tearDown() throws Exception {
		container.getService(Fenetre.class).waitUntilExit();
		Runtime.getRuntime().removeShutdownHook(container.getService(ThreadShutdown.class));
		container.destructor(true);
		System.out.println("\n\n");
	}

	
}
