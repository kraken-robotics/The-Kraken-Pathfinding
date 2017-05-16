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

import org.junit.Assert;
import org.junit.Test;
import utils.Vec2RO;
import obstacles.types.ObstacleCircular;
import obstacles.types.ObstacleRectangular;
import obstacles.types.ObstacleRobot;
import obstacles.types.ObstaclesFixes;

/**
 * Test unitaire des obstacles
 * 
 * @author pf
 *
 */

public class JUnit_Obstacle extends JUnit_Test
{

	@Test
	public void test_collision_cercle() throws Exception
	{
		ObstacleCircular o = new ObstacleCircular(new Vec2RO(0, 0), 30);
		Assert.assertTrue(o.squaredDistance(new Vec2RO(10, 10)) == 0);
		Assert.assertTrue(o.squaredDistance(new Vec2RO(22, 22)) > 0);
	}

	@Test
	public void test_collision_rectangle() throws Exception
	{
		ObstacleRectangular o = new ObstacleRectangular(new Vec2RO(0, 0), 30, 10, 0);
		Assert.assertTrue(o.squaredDistance(new Vec2RO(13, -3)) == 0);
		Assert.assertTrue(o.squaredDistance(new Vec2RO(16, 0)) > 0);
		Assert.assertTrue(o.squaredDistance(new Vec2RO(-16, 0)) > 0);
		Assert.assertTrue(o.squaredDistance(new Vec2RO(0, 7)) > 0);
		Assert.assertTrue(o.squaredDistance(new Vec2RO(0, -7)) > 0);
	}
	
	@Test
	public void test_collision_marge_obstacle_robot() throws Exception
	{
		ObstacleRobot o = new ObstacleRobot(50, 50, 100, 50);
		o.update(new Vec2RO(0,0), 0);
		ObstacleRobot.setMarge(true);
		Assert.assertTrue(o.squaredDistance(new Vec2RO(0, 60)) == 0);
		ObstacleRobot.setMarge(false);
		Assert.assertTrue(o.squaredDistance(new Vec2RO(0, 60)) > 0);
	}

	@Test
	public void test_collision_cercle_distance() throws Exception
	{
		ObstacleCircular o = new ObstacleCircular(new Vec2RO(0, 0), 10);
		Assert.assertTrue(o.isProcheObstacle(new Vec2RO(10, 10), 20));
		Assert.assertTrue(!o.isProcheObstacle(new Vec2RO(22, 22), 20));
	}

	@Test
	public void test_collision_rectangle_distance() throws Exception
	{
		ObstacleRectangular o = new ObstacleRectangular(new Vec2RO(0, 0), 25, 5, 0);
		Assert.assertTrue(o.isProcheObstacle(new Vec2RO(13, -3), 5));
		Assert.assertTrue(!o.isProcheObstacle(new Vec2RO(20, 0), 5));
		Assert.assertTrue(!o.isProcheObstacle(new Vec2RO(20, 0), 5));
		Assert.assertTrue(!o.isProcheObstacle(new Vec2RO(0, 8), 5));
		Assert.assertTrue(!o.isProcheObstacle(new Vec2RO(0, -8), 5));
	}

	// Obsolète
	/*
	 * @Test
	 * public void test_collision_segment_cercle() throws Exception
	 * {
	 * ObstacleCircular o = new ObstacleCircular(new Vec2(0, 50), 30);
	 * Assert.assertTrue(!o.obstacle_proximite_dans_segment(new Vec2(-100,0),
	 * new Vec2(100,0), 0));
	 * Assert.assertTrue(o.obstacle_proximite_dans_segment(new Vec2(-100,30),
	 * new Vec2(100,30), 0));
	 * Assert.assertTrue(!o.obstacle_proximite_dans_segment(new Vec2(-500,30),
	 * new Vec2(-400,30), 0));
	 * Assert.assertTrue(o.obstacle_proximite_dans_segment(new Vec2(-100,30),
	 * new Vec2(-20,30), 0));
	 * }
	 */

	@Test
	public void test_is_dans_obstacle_rectangle() throws Exception
	{
		ObstacleRectangular o = new ObstacleRectangular(new Vec2RO(0, 0), 200, 200, Math.PI / 8);
		Assert.assertTrue(o.squaredDistance(new Vec2RO(0, 0)) == 0);
		Assert.assertTrue(o.squaredDistance(new Vec2RO(100, 0)) == 0);
		Assert.assertTrue(o.squaredDistance(new Vec2RO(0, -100)) == 0);
		Assert.assertTrue(o.squaredDistance(new Vec2RO(90, 90)) > 0);
		Assert.assertTrue(o.squaredDistance(new Vec2RO(-90, -90)) > 0);
		Assert.assertTrue(o.squaredDistance(new Vec2RO(54, 130)) == 0);
		Assert.assertTrue(o.squaredDistance(new Vec2RO(-54, -130)) == 0);
		Assert.assertTrue(o.squaredDistance(new Vec2RO(-54, 130)) > 0);
		Assert.assertTrue(o.squaredDistance(new Vec2RO(54, -130)) > 0);
		Assert.assertTrue(o.squaredDistance(new Vec2RO(-100, 100)) > 0);
	}

	@Test
	public void test_collision_rectangles() throws Exception
	{
		ObstacleRectangular o = new ObstacleRectangular(new Vec2RO(1000, 1000), 200, 200, Math.PI / 8);
		Assert.assertTrue(!o.isColliding(new ObstacleRectangular(new Vec2RO(1200, 1000), 10, 10, 0)));
		Assert.assertTrue(!o.isColliding(new ObstacleRectangular(new Vec2RO(800, 1000), 10, 10, 0)));
		Assert.assertTrue(!o.isColliding(new ObstacleRectangular(new Vec2RO(0, 1200), 10, 10, 0)));
		Assert.assertTrue(!o.isColliding(new ObstacleRectangular(new Vec2RO(0, 800), 10, 10, 0)));
		Assert.assertTrue(!o.isColliding(new ObstacleRectangular(new Vec2RO(900, 1100), 20, 20, 0)));
		Assert.assertTrue(!o.isColliding(new ObstacleRectangular(new Vec2RO(900, 1100), 40, 40, 0)));
		Assert.assertTrue(o.isColliding(new ObstacleRectangular(new Vec2RO(900, 1100), 60, 60, 0)));
	}

	@Test
	public void test_collision_robot_bord() throws Exception
	{
		ObstacleRectangular o = new ObstacleRectangular(new Vec2RO(1320, 250), 250, 360, Math.PI / 6);
		Assert.assertTrue(ObstaclesFixes.BORD_DROITE.getObstacle().isColliding(o));
	}

}
