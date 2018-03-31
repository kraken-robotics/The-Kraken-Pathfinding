/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;

import pfg.config.Config;
import pfg.graphic.GraphicDisplay;
import pfg.injector.Injector;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.Kraken;
import pfg.kraken.LogCategoryKraken;
import pfg.kraken.obstacles.CircularObstacle;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
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
	protected GraphicDisplay display;
	
	@Rule
	public TestName testName = new TestName();

	public void setUpStandard(String... profiles) throws Exception
	{
		System.out.println("----- TEST START : " + testName.getMethodName() + " -----");
		List<Obstacle> obs = new ArrayList<Obstacle>();
		
		obs.add(new RectangularObstacle(new XY(-(1140 - 350 / 2), 2000 - 360 / 2), 350, 360));
		obs.add(new RectangularObstacle(new XY(-(1500 - 360 / 2), 2000 - 360 / 2), 360, 360));
		obs.add(new RectangularObstacle(new XY(-(790 - 360 / 2), 2000 - 360 / 2), 360, 360));
		
		obs.add(new RectangularObstacle(new XY(1140 - 350 / 2, 2000 - 360 / 2), 350, 360));
		obs.add(new RectangularObstacle(new XY(1500 - 360 / 2, 2000 - 360 / 2), 360, 360	));
		obs.add(new RectangularObstacle(new XY(790 - 360 / 2, 2000 - 360 / 2), 360, 360));

		obs.add(new RectangularObstacle(new XY(1500 - 710 / 2, 2000 - 360 - 11), 710, 22));
		obs.add(new RectangularObstacle(new XY(-1500 + 710 / 2, 2000 - 360 - 11), 710, 22));

		obs.add(new RectangularObstacle(new XY(54 - 1500, 1075), 108, 494));
		obs.add(new RectangularObstacle(new XY(1500 - 54, 1075), 108, 494));

		obs.add(new CircularObstacle(new XY(0, 0), 200));

		obs.add(new RectangularObstacle(new XY(0, 500).rotateNewVector(-Math.PI / 4, new XY(0, 0)), 140, 600, -Math.PI / 4));
		obs.add(new RectangularObstacle(new XY(0, 500), 140, 600));
		obs.add(new RectangularObstacle(new XY(0, 500).rotateNewVector(Math.PI / 4, new XY(0, 0)), 140, 600, Math.PI / 4));

		obs.add(new CircularObstacle(new XY(-1500, 0), 540));
		obs.add(new CircularObstacle(new XY(1500, 0), 540));

		obs.add(new CircularObstacle(new XY(-350, 1960), 40));
		obs.add(new CircularObstacle(new XY(350, 1960), 40));

		obs.add(new CircularObstacle(new XY(-1460, 650), 40));
		obs.add(new CircularObstacle(new XY(1460, 650), 40));
		
		obs.add(new CircularObstacle(new XY(650 - 1500, 2000 - 555), 125));
		obs.add(new CircularObstacle(new XY(1500 - 650, 2000 - 555), 125));

		obs.add(new CircularObstacle(new XY(1070 - 1500, 2000 - 1870), 125));
		obs.add(new CircularObstacle(new XY(1500 - 1070, 2000 - 1870), 125));

		obs.add(new CircularObstacle(new XY(200 - 1500, 1400), 32));
		obs.add(new CircularObstacle(new XY(1500 - 200, 1400), 32));
		obs.add(new CircularObstacle(new XY(1000 - 1500, 1400), 32));
		obs.add(new CircularObstacle(new XY(1500 - 1000, 1400), 32));
		obs.add(new CircularObstacle(new XY(500 - 1500, 900), 32));
		obs.add(new CircularObstacle(new XY(1500 - 500, 900), 32));
		obs.add(new CircularObstacle(new XY(900 - 1500, 600), 32));
		obs.add(new CircularObstacle(new XY(1500 - 900, 600), 32));
		obs.add(new CircularObstacle(new XY(800 - 1500, 150), 32));
		obs.add(new CircularObstacle(new XY(1500 - 800, 150), 32));

		RectangularObstacle robot = new RectangularObstacle(250, 80, 110, 110);

		kraken = new Kraken(robot, obs, new XY(-1500,0), new XY(1500, 2000), "kraken-test.conf", profiles);
		init(kraken);
	}
	
	private void init(Kraken kraken) throws Exception
	{
		Method m = Kraken.class.getDeclaredMethod("getInjector");
		m.setAccessible(true);
		injector = (Injector) m.invoke(kraken);
		config = injector.getService(Config.class);
		log = injector.getService(Log.class);
		display = kraken.getGraphicDisplay();
		log.write("Test unitaire : " + testName.getMethodName(), LogCategoryKraken.TEST);
		
	}
	
	public void setUpWith(List<Obstacle> fixedObstacles, String... profiles) throws Exception
	{
		System.out.println("----- TEST START : " + testName.getMethodName() + " -----");
		RectangularObstacle robot = new RectangularObstacle(250, 80, 110, 110, 0); 

		kraken = new Kraken(robot, fixedObstacles, new XY(-1500, 0), new XY(1500, 2000), "kraken-test.conf", profiles);
		init(kraken);
	}

	@After
	public void tearDown() throws Exception
	{
		if(config.getBoolean(ConfigInfoKraken.GRAPHIC_ENABLE))
		{
			display.refresh();
			Thread.sleep(1000);
		}
	}
	
	/**
	 * Lanceur d'une seule méthode de test
	 * 
	 * @param args
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws ClassNotFoundException
	{
		if(args.length != 2)
		{
			System.out.println("Usage : JUnit_Test class method");
		}
		else
		{
			Request request = Request.method(Class.forName(args[0]), args[1]);
	
			Result result = new JUnitCore().run(request);
			System.exit(result.wasSuccessful() ? 0 : 1);
		}
	}

}
