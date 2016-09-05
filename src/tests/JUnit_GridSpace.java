package tests;

import java.util.ArrayList;

import obstacles.types.ObstacleProximity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pathfinding.dstarlite.Direction;
import pathfinding.dstarlite.GridSpace;
import container.ServiceNames;
import utils.ConfigInfo;
import utils.Vec2;
import utils.permissions.ReadOnly;

/**
 * Tests unitaires de GridSpace
 * @author pf
 *
 */

public class JUnit_GridSpace extends JUnit_Test {

	private GridSpace gridspace;
	
	@Override
	@Before
    public void setUp() throws Exception {
        super.setUp();
        gridspace = (GridSpace) container.getService(ServiceNames.GRID_SPACE);
    }
	
	@Test
	public void test_computeVec2() throws Exception
	{
		log.debug(GridSpace.computeVec2(0));
		log.debug(GridSpace.computeVec2(64));
		log.debug(GridSpace.computeVec2(63));
		Assert.assertTrue(GridSpace.computeVec2(0).equals(new Vec2<ReadOnly>(-1500, 0)));
		Assert.assertTrue(GridSpace.computeVec2(0).equals(new Vec2<ReadOnly>(-1500, 0)));
	}

	@Test
	public void test_distance() throws Exception
	{
		Assert.assertTrue(gridspace.distanceStatique(523, Direction.NE) == 1414);
		Assert.assertTrue(gridspace.distanceStatique(523, Direction.N) == 1000);
		Assert.assertTrue(gridspace.distanceStatique(1, Direction.E) == Integer.MAX_VALUE);
	}

	@Test
	public void test_distanceHeuristique() throws Exception
	{
		Assert.assertTrue(GridSpace.distanceHeuristiqueDStarLite(1, 2) == 1000);
		Assert.assertTrue(GridSpace.distanceHeuristiqueDStarLite(1, 65) == 1000);
		Assert.assertTrue(GridSpace.distanceHeuristiqueDStarLite(1, 64) == 1414);
		Assert.assertTrue(GridSpace.distanceHeuristiqueDStarLite(64, 1) == 1414);
		Assert.assertTrue(GridSpace.distanceHeuristiqueDStarLite(1, 1+2*64) == 2000);
		Assert.assertTrue(GridSpace.distanceHeuristiqueDStarLite(1+2*64, 1) == 2000);
		Assert.assertTrue(GridSpace.distanceHeuristiqueDStarLite(64, 63) == 63414);
		Assert.assertTrue(GridSpace.distanceHeuristiqueDStarLite(63, 64) == 63414);
	}
	
	@Test
	public void test_getGridPointVoisin() throws Exception
	{
		for(int i = 0; i < 8; i++)
		{
			log.debug(i);
			Assert.assertEquals((i<4?1414:1000), GridSpace.distanceHeuristiqueDStarLite(GridSpace.getGridPointVoisin(150, Direction.values()[i]), 150));
		}

		Assert.assertEquals(-1, GridSpace.getGridPointVoisin(21, Direction.S));
		Assert.assertEquals(-1, GridSpace.getGridPointVoisin(63, Direction.SE));
		Assert.assertEquals(-1, GridSpace.getGridPointVoisin(127, Direction.E));
		Assert.assertEquals(-1, GridSpace.getGridPointVoisin(128, Direction.O));
		Assert.assertEquals(-1, GridSpace.getGridPointVoisin(21, Direction.SE));
	}
	
	@Test
	public void test_computeGridPoint() throws Exception
	{
		for(int i = 0; i < GridSpace.NB_POINTS; i++)
			Assert.assertTrue(GridSpace.computeGridPoint(GridSpace.computeVec2(i)) == i);
	}
	
	@Test
	public void test_ajout_obstacle() throws Exception
	{
    	int peremption = config.getInt(ConfigInfo.DUREE_PEREMPTION_OBSTACLES);
		Assert.assertTrue(gridspace.startNewPathfinding().isEmpty());
		ArrayList<ObstacleProximity>[] b = gridspace.getOldAndNewObstacles();
		Assert.assertTrue(b[0].isEmpty());
		Assert.assertTrue(b[1].isEmpty());
		gridspace.addObstacleAndRemoveNearbyObstacles(new Vec2<ReadOnly>(200, 100));
		b = gridspace.getOldAndNewObstacles();
		Assert.assertTrue(b[0].isEmpty());
		Assert.assertTrue(!b[1].isEmpty());
		gridspace.addObstacleAndRemoveNearbyObstacles(new Vec2<ReadOnly>(210, 100));
		b = gridspace.getOldAndNewObstacles();
		Assert.assertEquals(b[0].size(), 1); // on a supprimé l'autre qui était trop proche
		Assert.assertEquals(b[1].size(), 1);
		gridspace.addObstacleAndRemoveNearbyObstacles(new Vec2<ReadOnly>(400, 100));
		b = gridspace.getOldAndNewObstacles();
		Assert.assertTrue(b[0].isEmpty());
		Assert.assertTrue(!b[1].isEmpty());
		b = gridspace.getOldAndNewObstacles();
		Assert.assertTrue(b[0].isEmpty());
		Assert.assertTrue(b[1].isEmpty());
		Thread.sleep(peremption+10);
		log.debug("Ça commence");
		b = gridspace.getOldAndNewObstacles();
		log.debug("Old : ");
		for(ObstacleProximity o : b[0])
			log.debug(o.getDeathDate());
		log.debug("New : ");
		for(ObstacleProximity o : b[1])
			log.debug(o.getDeathDate());

		Assert.assertTrue(!b[0].isEmpty());
		Assert.assertTrue(b[1].isEmpty());
	}

}
