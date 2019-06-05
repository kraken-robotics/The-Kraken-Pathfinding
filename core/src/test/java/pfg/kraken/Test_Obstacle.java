/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pfg.kraken.obstacles.CircularObstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.struct.XY;

/**
 * Test unitaire des obstacles
 * 
 * @author pf
 *
 */

public class Test_Obstacle extends JUnit_Test
{
	@Before
	public void setUp() throws Exception
	{
		super.setUpWith(null, "default");
	}

	@Test
	public void test_collision_cercle() throws Exception
	{
		CircularObstacle o = new CircularObstacle(new XY(0, 0), 30);
		Assert.assertTrue(o.squaredDistance(new XY(10, 10)) == 0);
		Assert.assertTrue(o.squaredDistance(new XY(22, 22)) > 0);
	}

	@Test
	public void test_collision_rectangle() throws Exception
	{
		RectangularObstacle o = new RectangularObstacle(new XY(0, 0), 30, 10, 0);
		Assert.assertTrue(o.squaredDistance(new XY(13, -3)) == 0);
		Assert.assertTrue(o.squaredDistance(new XY(16, 0)) > 0);
		Assert.assertTrue(o.squaredDistance(new XY(-16, 0)) > 0);
		Assert.assertTrue(o.squaredDistance(new XY(0, 7)) > 0);
		Assert.assertTrue(o.squaredDistance(new XY(0, -7)) > 0);
	}

	@Test
	public void test_collision_cercle_distance() throws Exception
	{
		CircularObstacle o = new CircularObstacle(new XY(0, 0), 10);
		Assert.assertTrue(o.isProcheObstacle(new XY(10, 10), 20));
		Assert.assertTrue(!o.isProcheObstacle(new XY(22, 22), 20));
	}

	@Test
	public void test_collision_rectangle_distance() throws Exception
	{
		RectangularObstacle o = new RectangularObstacle(new XY(0, 0), 25, 5, 0);
		Assert.assertTrue(o.isProcheObstacle(new XY(13, -3), 5));
		Assert.assertTrue(!o.isProcheObstacle(new XY(20, 0), 5));
		Assert.assertTrue(!o.isProcheObstacle(new XY(20, 0), 5));
		Assert.assertTrue(!o.isProcheObstacle(new XY(0, 8), 5));
		Assert.assertTrue(!o.isProcheObstacle(new XY(0, -8), 5));
	}

	@Test
	public void test_is_dans_obstacle_rectangle() throws Exception
	{
		RectangularObstacle o = new RectangularObstacle(new XY(0, 0), 200, 200, Math.PI / 8);
		Assert.assertTrue(o.squaredDistance(new XY(0, 0)) == 0);
		Assert.assertTrue(o.squaredDistance(new XY(100, 0)) == 0);
		Assert.assertTrue(o.squaredDistance(new XY(0, -100)) == 0);
		Assert.assertTrue(o.squaredDistance(new XY(90, 90)) > 0);
		Assert.assertTrue(o.squaredDistance(new XY(-90, -90)) > 0);
		Assert.assertTrue(o.squaredDistance(new XY(54, 130)) == 0);
		Assert.assertTrue(o.squaredDistance(new XY(-54, -130)) == 0);
		Assert.assertTrue(o.squaredDistance(new XY(-54, 130)) > 0);
		Assert.assertTrue(o.squaredDistance(new XY(54, -130)) > 0);
		Assert.assertTrue(o.squaredDistance(new XY(-100, 100)) > 0);
	}

	@Test
	public void test_collision_rectangles() throws Exception
	{
		RectangularObstacle o = new RectangularObstacle(new XY(1000, 1000), 200, 200, Math.PI / 8);
		Assert.assertTrue(!o.isColliding(new RectangularObstacle(new XY(1200, 1000), 10, 10, 0)));
		Assert.assertTrue(!o.isColliding(new RectangularObstacle(new XY(800, 1000), 10, 10, 0)));
		Assert.assertTrue(!o.isColliding(new RectangularObstacle(new XY(0, 1200), 10, 10, 0)));
		Assert.assertTrue(!o.isColliding(new RectangularObstacle(new XY(0, 800), 10, 10, 0)));
		Assert.assertTrue(!o.isColliding(new RectangularObstacle(new XY(900, 1100), 20, 20, 0)));
		Assert.assertTrue(!o.isColliding(new RectangularObstacle(new XY(900, 1100), 40, 40, 0)));
		Assert.assertTrue(o.isColliding(new RectangularObstacle(new XY(900, 1100), 60, 60, 0)));
	}

}
