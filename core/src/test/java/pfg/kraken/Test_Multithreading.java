/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pfg.graphic.printable.Layer;
import pfg.kraken.astar.TentacularAStar;
import pfg.kraken.exceptions.PathfindingException;
import pfg.kraken.obstacles.CircularObstacle;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.robot.ItineraryPoint;
import pfg.kraken.utils.XY;
import pfg.kraken.utils.XYO;
import pfg.kraken.utils.XY_RW;

/**
 * Some multithreading test
 * @author Pierre-François Gimenez
 *
 */

public class Test_Multithreading extends JUnit_Test
{
	protected TentacularAStar astar;
	
	@Before
	public void setUp() throws Exception
	{
		List<Obstacle> obs = new ArrayList<Obstacle>();
		obs.add(new RectangularObstacle(new XY_RW(50,1050), 500, 500));
		obs.add(new RectangularObstacle(new XY_RW(400,200), 200, 200));
		obs.add(new RectangularObstacle(new XY_RW(-1000,1050), 200, 200));
		obs.add(new RectangularObstacle(new XY_RW(100,410), 200, 200));
		obs.add(new RectangularObstacle(new XY_RW(-600,300), 200, 200));
		obs.add(new RectangularObstacle(new XY_RW(-1000,1900), 200, 200));
		super.setUpWith(obs, "default", "graphic", "multithreading");
		astar = injector.getService(TentacularAStar.class);
	}

	@Test
	public void test_multithreading() throws Exception
	{
		List<Obstacle> obs = new ArrayList<Obstacle>();
		obs.add(new RectangularObstacle(new XY(800,200), 200, 200));
		obs.add(new RectangularObstacle(new XY(800,300), 200, 200));
		obs.add(new RectangularObstacle(new XY(-800,1200), 100, 200));
		obs.add(new RectangularObstacle(new XY(-1000,300), 500, 500));
		obs.add(new RectangularObstacle(new XY(200,1600), 800, 300));
		obs.add(new RectangularObstacle(new XY(1450,700), 300, 100));
		obs.add(new CircularObstacle(new XY(500,600), 100));
		
		try
		{
			kraken.initializeNewSearch(new XYO(800, 200, 0), new XY(1000, 900));

			List<ItineraryPoint> path = kraken.search();
			
			System.out.println("Here is the trajectory :");
			ItineraryPoint previous = null;
			for(ItineraryPoint p : path)
			{
				display.addPrintable(p, Color.BLACK, Layer.FOREGROUND.layer);
				System.out.println(p);
				if(previous != null)
					Assert.assertTrue((previous.x - p.x) * (previous.x - p.x) + (previous.y - p.y) * (previous.y - p.y) <= 27*27);
				previous = p;
			}

			display.refresh();
		}
		catch(PathfindingException e)
		{
			e.printStackTrace();
		}
	}

}