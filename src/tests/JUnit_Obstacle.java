package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import container.ServiceNames;
import utils.Vec2;
import obstacles.types.ObstacleCircular;
import obstacles.types.ObstacleRectangular;
import obstacles.types.ObstacleTrajectoireCourbe;
import obstacles.types.ObstaclesFixes;
import pathfinding.dstarlite.GridSpace;
import pathfinding.thetastar.LocomotionArc;
import pathfinding.thetastar.RayonCourbure;
import permissions.ReadOnly;

/**
 * Test unitaire des obstacles
 * @author pf
 *
 */

public class JUnit_Obstacle extends JUnit_Test {

	private GridSpace gridspace;
	
	@Before
    public void setUp() throws Exception {
        super.setUp();
        gridspace = (GridSpace) container.getService(ServiceNames.GRID_SPACE);
    }
	
    @Test
    public void test_collision_cercle() throws Exception
    {
    	ObstacleCircular o = new ObstacleCircular(new Vec2<ReadOnly>(0, 0), 30);
    	Assert.assertTrue(o.isInObstacle(new Vec2<ReadOnly>(10,10)));
    	Assert.assertTrue(!o.isInObstacle(new Vec2<ReadOnly>(22,22)));
    }

    @Test
    public void test_collision_rectangle() throws Exception
    {
    	ObstacleRectangular o = new ObstacleRectangular(new Vec2<ReadOnly>(0, 0), 30, 10, 0);
    	Assert.assertTrue(o.isInObstacle(new Vec2<ReadOnly>(13,-3)));
    	Assert.assertTrue(!o.isInObstacle(new Vec2<ReadOnly>(16,0)));
    	Assert.assertTrue(!o.isInObstacle(new Vec2<ReadOnly>(-16,0)));
    	Assert.assertTrue(!o.isInObstacle(new Vec2<ReadOnly>(0,7)));
    	Assert.assertTrue(!o.isInObstacle(new Vec2<ReadOnly>(0,-7)));
    }

    @Test
    public void test_collision_cercle_distance() throws Exception
    {
    	ObstacleCircular o = new ObstacleCircular(new Vec2<ReadOnly>(0, 0), 10);
    	Assert.assertTrue(o.isProcheObstacle(new Vec2<ReadOnly>(10,10), 20));
    	Assert.assertTrue(!o.isProcheObstacle(new Vec2<ReadOnly>(22,22), 20));
    }

    @Test
    public void test_collision_rectangle_distance() throws Exception
    {
    	ObstacleRectangular o = new ObstacleRectangular(new Vec2<ReadOnly>(0, 0), 25, 5);
    	Assert.assertTrue(o.isProcheObstacle(new Vec2<ReadOnly>(13,-3),5));
    	Assert.assertTrue(!o.isProcheObstacle(new Vec2<ReadOnly>(20,0),5));
    	Assert.assertTrue(!o.isProcheObstacle(new Vec2<ReadOnly>(20,0),5));
    	Assert.assertTrue(!o.isProcheObstacle(new Vec2<ReadOnly>(0,8),5));
    	Assert.assertTrue(!o.isProcheObstacle(new Vec2<ReadOnly>(0,-8),5));
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
    	ObstacleRectangular o = new ObstacleRectangular(new Vec2<ReadOnly>(0,0), 200, 200, Math.PI/8);
    	Assert.assertTrue(o.isInObstacle(new Vec2<ReadOnly>(0,0)));
    	Assert.assertTrue(o.isInObstacle(new Vec2<ReadOnly>(100,0)));
    	Assert.assertTrue(o.isInObstacle(new Vec2<ReadOnly>(0,-100)));
    	Assert.assertTrue(!o.isInObstacle(new Vec2<ReadOnly>(90,90)));
    	Assert.assertTrue(!o.isInObstacle(new Vec2<ReadOnly>(-90,-90)));
    	Assert.assertTrue(o.isInObstacle(new Vec2<ReadOnly>(54,130)));
    	Assert.assertTrue(o.isInObstacle(new Vec2<ReadOnly>(-54,-130)));
    	Assert.assertTrue(!o.isInObstacle(new Vec2<ReadOnly>(-54,130)));
    	Assert.assertTrue(!o.isInObstacle(new Vec2<ReadOnly>(54,-130)));
    	Assert.assertTrue(!o.isInObstacle(new Vec2<ReadOnly>(-100,100)));
    }
    
    @Test
    public void test_obstacle_trajectoire_courbe() throws Exception
    {
//    	new ObstacleRectangular(new Vec2<ReadOnly>(-500, 500), 400, 400, 0);
 //   	new ObstacleRectangular(new Vec2<ReadOnly>(-500, 500), 400, 400, Math.PI/12);
  //  	new ObstacleRectangular(new Vec2<ReadOnly>(-500, 500), 400, 400, Math.PI/6);
   // 	new ObstacleRectangular(new Vec2<ReadOnly>(-500, 500), 400, 400, Math.PI/4);
   // 	new ObstacleRectangular(new Vec2<ReadOnly>(-500, 500), 400, 400, Math.PI/2);
    	LocomotionArc arc = new LocomotionArc();
    	arc.update(gridspace, gridspace.computeGridPoint(new Vec2<ReadOnly>(-500, 500)), RayonCourbure.EXEMPLE_1, gridspace.computeGridPoint(new Vec2<ReadOnly>(1000, 1300)), Math.PI/2);
    	new ObstacleTrajectoireCourbe(arc);
    	arc.update(gridspace, gridspace.computeGridPoint(new Vec2<ReadOnly>(-500, 500)), RayonCourbure.EXEMPLE_2, gridspace.computeGridPoint(new Vec2<ReadOnly>(1000, 1300)), Math.PI/2);
    	new ObstacleTrajectoireCourbe(arc);
    }
    @Test
    public void test_collision_rectangles() throws Exception
    {
    	ObstacleRectangular o = new ObstacleRectangular(new Vec2<ReadOnly>(1000,1000), 200, 200, Math.PI/8);
    	Assert.assertTrue(!o.isColliding(new ObstacleRectangular(new Vec2<ReadOnly>(1200,1000), 10, 10, 0)));
    	Assert.assertTrue(!o.isColliding(new ObstacleRectangular(new Vec2<ReadOnly>(800, 1000), 10, 10, 0)));
    	Assert.assertTrue(!o.isColliding(new ObstacleRectangular(new Vec2<ReadOnly>(0, 1200), 10, 10, 0)));
    	Assert.assertTrue(!o.isColliding(new ObstacleRectangular(new Vec2<ReadOnly>(0, 800), 10, 10, 0)));
    	Assert.assertTrue(!o.isColliding(new ObstacleRectangular(new Vec2<ReadOnly>(900, 1100), 20, 20, 0)));
    	Assert.assertTrue(!o.isColliding(new ObstacleRectangular(new Vec2<ReadOnly>(900, 1100), 40, 40, 0)));
    	Assert.assertTrue(o.isColliding(new ObstacleRectangular(new Vec2<ReadOnly>(900, 1100), 60, 60, 0)));
    }

    @Test
    public void test_collision_robot_bord() throws Exception
    {
    	ObstacleRectangular o = new ObstacleRectangular(new Vec2<ReadOnly>(1320, 250), 250, 360, Math.PI/6);
    	Assert.assertTrue(o.isColliding(ObstaclesFixes.BORD_DROITE.getObstacle()));
    }

}
