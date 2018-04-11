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
import pfg.kraken.astar.DirectionStrategy;
import pfg.kraken.astar.TentacularAStar;
import pfg.kraken.exceptions.NoPathException;
import pfg.kraken.exceptions.NotInitializedPathfindingException;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.robot.Cinematique;
import pfg.kraken.robot.ItineraryPoint;
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

	@Test(expected=NoPathException.class)
	public void test_out_of_bounds() throws Exception
	{
		pathfinding.initializeNewSearch(new Cinematique(new XYO(0, 200, 0)), new Cinematique(new XYO(10000, 10000, 0)), DirectionStrategy.FASTEST, "XY", null);
	}
	
	@Test(expected=NoPathException.class)
	public void test_start_inside_obstacle() throws Exception
	{
		pathfinding.initializeNewSearch(new Cinematique(new XYO(50,1050, 0)), new Cinematique(new XYO(0, 200, 0)), DirectionStrategy.FASTEST, "XY", null);
	}
	
	@Test(expected=NoPathException.class)
	public void test_finish_inside_obstacle() throws Exception
	{
		pathfinding.initializeNewSearch(new Cinematique(new XYO(0, 200, 0)), new Cinematique(new XYO(50,1050, 0)), DirectionStrategy.FASTEST, "XY", null);
	}
	
	@Test
	public void test_exemple_1() throws Exception
	{
		pathfinding.initializeNewSearch(new Cinematique(new XYO(0, 200, 0)), new Cinematique(new XYO(1000, 1000, 0)), DirectionStrategy.FASTEST, "XY", null);

		List<ItineraryPoint> path = pathfinding.search();
		
		System.out.println("Here is the trajectory :");
		for(ItineraryPoint p : path)
		{
			display.addPrintable(p, Color.BLACK, Layer.FOREGROUND.layer);
			System.out.println(p);
		}
	}
	
	@Test(expected=NotInitializedPathfindingException.class)
	public void test_no_initialization() throws Exception
	{
		pathfinding.search();
	}
	
}
