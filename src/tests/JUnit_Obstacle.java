package tests;

import org.junit.Assert;
import org.junit.Test;

import utils.Vec2;
import obstacles.ObstacleCircular;
import obstacles.ObstacleRectangular;

/**
 * Test unitaire des obstacles
 * @author pf
 *
 */

public class JUnit_Obstacle extends JUnit_Test {

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
    	ObstacleRectangular o = new ObstacleRectangular(new Vec2(0, 0), 30, 10, 0);
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

    // Obsolète
/*    @Test
    public void test_collision_segment_cercle() throws Exception
    {
    	ObstacleCircular o = new ObstacleCircular(new Vec2(0, 50), 30);
    	Assert.assertTrue(!o.obstacle_proximite_dans_segment(new Vec2(-100,0), new Vec2(100,0), 0));
    	Assert.assertTrue(o.obstacle_proximite_dans_segment(new Vec2(-100,30), new Vec2(100,30), 0));
    	Assert.assertTrue(!o.obstacle_proximite_dans_segment(new Vec2(-500,30), new Vec2(-400,30), 0));
    	Assert.assertTrue(o.obstacle_proximite_dans_segment(new Vec2(-100,30), new Vec2(-20,30), 0));
    }*/
    
    @Test
    public void test_is_dans_obstacle_rectangle() throws Exception
    {
    	ObstacleRectangular o = new ObstacleRectangular(new Vec2(0,0), 200, 200, Math.PI/8);
    	Assert.assertTrue(o.isInObstacle(new Vec2(0,0)));
    	Assert.assertTrue(o.isInObstacle(new Vec2(100,0)));
    	Assert.assertTrue(o.isInObstacle(new Vec2(0,-100)));
    	Assert.assertTrue(!o.isInObstacle(new Vec2(90,90)));
    	Assert.assertTrue(!o.isInObstacle(new Vec2(-90,-90)));
    	Assert.assertTrue(o.isInObstacle(new Vec2(54,130)));
    	Assert.assertTrue(o.isInObstacle(new Vec2(-54,-130)));
    	Assert.assertTrue(!o.isInObstacle(new Vec2(-54,130)));
    	Assert.assertTrue(!o.isInObstacle(new Vec2(54,-130)));
    	Assert.assertTrue(!o.isInObstacle(new Vec2(-100,100)));
    }
    
    @Test
    public void test_collision_rectangles() throws Exception
    {
    	ObstacleRectangular o = new ObstacleRectangular(new Vec2(1000,1000), 200, 200, Math.PI/8);
    	Assert.assertTrue(!o.isColliding(new ObstacleRectangular(new Vec2(1200,1000), 10, 10, 0)));
    	Assert.assertTrue(!o.isColliding(new ObstacleRectangular(new Vec2(800, 1000), 10, 10, 0)));
    	Assert.assertTrue(!o.isColliding(new ObstacleRectangular(new Vec2(0, 1200), 10, 10, 0)));
    	Assert.assertTrue(!o.isColliding(new ObstacleRectangular(new Vec2(0, 800), 10, 10, 0)));
    	Assert.assertTrue(!o.isColliding(new ObstacleRectangular(new Vec2(900, 1100), 20, 20, 0)));
    	Assert.assertTrue(!o.isColliding(new ObstacleRectangular(new Vec2(900, 1100), 50, 50, 0)));
    	Assert.assertTrue(!o.isColliding(new ObstacleRectangular(new Vec2(900, 1100), 60, 60, 0)));
    }

}
