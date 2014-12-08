package tests;

import org.junit.Assert;
import org.junit.Test;

import smartMath.Vec2;
import obstacles.ObstacleCircular;
import obstacles.ObstacleRectangular;

/**
 * Test unitaire des obstacles
 * @author pf
 *
 */

public class JUnit_Obstacle {

    @Test
    public void test_collision_cercle() throws Exception
    {
    	ObstacleCircular o = new ObstacleCircular(new Vec2(0, 0), 30);
    	Assert.assertTrue(o.isInObstacle(new Vec2(10,10)));
    	Assert.assertTrue(!o.isInObstacle(new Vec2(22,22)));
    }

    @Test
    public void test_collision_rectangle() throws Exception
    {
    	ObstacleRectangular o = new ObstacleRectangular(new Vec2(0, 0), 30, 10);
    	Assert.assertTrue(o.isInObstacle(new Vec2(13,-3)));
    	Assert.assertTrue(!o.isInObstacle(new Vec2(16,0)));
    	Assert.assertTrue(!o.isInObstacle(new Vec2(-16,0)));
    	Assert.assertTrue(!o.isInObstacle(new Vec2(0,7)));
    	Assert.assertTrue(!o.isInObstacle(new Vec2(0,-7)));
    }

    @Test
    public void test_collision_cercle_distance() throws Exception
    {
    	ObstacleCircular o = new ObstacleCircular(new Vec2(0, 0), 10);
    	Assert.assertTrue(o.isProcheObstacle(new Vec2(10,10), 20));
    	Assert.assertTrue(!o.isProcheObstacle(new Vec2(22,22), 20));
    }

    @Test
    public void test_collision_rectangle_distance() throws Exception
    {
    	ObstacleRectangular o = new ObstacleRectangular(new Vec2(0, 0), 25, 5);
    	Assert.assertTrue(o.isProcheObstacle(new Vec2(13,-3),5));
    	Assert.assertTrue(!o.isProcheObstacle(new Vec2(20,0),5));
    	Assert.assertTrue(!o.isProcheObstacle(new Vec2(20,0),5));
    	Assert.assertTrue(!o.isProcheObstacle(new Vec2(0,8),5));
    	Assert.assertTrue(!o.isProcheObstacle(new Vec2(0,-8),5));
    }

    @Test
    public void test_collision_segment_cercle() throws Exception
    {
    	ObstacleCircular o = new ObstacleCircular(new Vec2(0, 50), 30);
    	Assert.assertTrue(!o.obstacle_proximite_dans_segment(new Vec2(-100,0), new Vec2(100,0), 0));
    	Assert.assertTrue(o.obstacle_proximite_dans_segment(new Vec2(-100,30), new Vec2(100,30), 0));
    	Assert.assertTrue(!o.obstacle_proximite_dans_segment(new Vec2(-500,30), new Vec2(-400,30), 0));
    	Assert.assertTrue(o.obstacle_proximite_dans_segment(new Vec2(-100,30), new Vec2(-20,30), 0));
    }

}
