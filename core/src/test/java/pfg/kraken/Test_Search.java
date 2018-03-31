/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import pfg.graphic.printable.Layer;
import pfg.kraken.astar.TentacularAStar;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.robot.ItineraryPoint;
import pfg.kraken.utils.XY;
import pfg.kraken.utils.XYO;
import pfg.kraken.utils.XY_RW;

/**
 * Various test on search
 * @author pf
 *
 */

public class Test_Search extends JUnit_Test
{
	private TentacularAStar pathfinding;

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
		super.setUpWith(obs, "default", "graphic");
		pathfinding = injector.getService(TentacularAStar.class);
	}

	@Test
	public void test_exemple_1() throws Exception
	{
		pathfinding.initializeNewSearch(new XYO(0, 200, 0), new XY(1000, 1000));

		List<ItineraryPoint> path = pathfinding.search();
		
		System.out.println("Here is the trajectory :");
		for(ItineraryPoint p : path)
		{
			display.addPrintable(p, Color.BLACK, Layer.FOREGROUND.layer);
			System.out.println(p);
		}
	}
	
}
