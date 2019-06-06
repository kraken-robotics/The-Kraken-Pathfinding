/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import pfg.kraken.astar.DirectionStrategy;
import pfg.kraken.astar.TentacularAStar;
import pfg.kraken.exceptions.NoPathException;
import pfg.kraken.exceptions.NotInitializedException;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.struct.Kinematic;
import pfg.kraken.struct.XYO;
import pfg.kraken.struct.XY_RW;

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
		super.setUpWith(obs, "default");
		pathfinding = injector.getService(TentacularAStar.class);
	}

	@Test(expected=NoPathException.class)
	public void test_out_of_bounds() throws Exception
	{
		pathfinding.initializeNewSearch(new Kinematic(new XYO(0, 200, 0)), new Kinematic(new XYO(10000, 10000, 0)), DirectionStrategy.FASTEST, "XY", null, 3000);
	}
	
	@Test(expected=NoPathException.class)
	public void test_start_inside_obstacle() throws Exception
	{
		pathfinding.initializeNewSearch(new Kinematic(new XYO(50,1050, 0)), new Kinematic(new XYO(0, 200, 0)), DirectionStrategy.FASTEST, "XY", null, 3000);
	}
	
	@Test(expected=NoPathException.class)
	public void test_finish_inside_obstacle() throws Exception
	{
		pathfinding.initializeNewSearch(new Kinematic(new XYO(0, 200, 0)), new Kinematic(new XYO(50,1050, 0)), DirectionStrategy.FASTEST, "XY", null, 3000);
	}
	
	@Test
	public void test_exemple_1() throws Exception
	{
		pathfinding.initializeNewSearch(new Kinematic(new XYO(1000, 200, 0)), new Kinematic(new XYO(1000, 1000, 0)), DirectionStrategy.FASTEST, "XY", null, 3000);
		pathfinding.searchWithoutReplanning();
	}
	
	@Test(expected=NotInitializedException.class)
	public void test_no_initialization() throws Exception
	{
		pathfinding.searchWithoutReplanning();
	}
	
}
