package tests;

import java.util.ArrayList;

import obstacles.types.ObstacleProximity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pathfinding.dstarlite.gridspace.Direction;
import pathfinding.dstarlite.gridspace.GridSpace;
import pathfinding.dstarlite.gridspace.PointDirige;
import pathfinding.dstarlite.gridspace.PointGridSpace;
import pathfinding.dstarlite.gridspace.PointGridSpaceManager;
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
	private PointGridSpaceManager pointManager;
	
	@Override
	@Before
    public void setUp() throws Exception {
        super.setUp();
        gridspace = container.getService(GridSpace.class);
        pointManager = container.getService(PointGridSpaceManager.class);
    }
	
	@Test
	public void test_computeVec2() throws Exception
	{
		log.debug(PointGridSpace.computeVec2(pointManager.get(0)));
		log.debug(PointGridSpace.computeVec2(pointManager.get(64)));
		log.debug(PointGridSpace.computeVec2(pointManager.get(63)));
		Assert.assertTrue(PointGridSpace.computeVec2(pointManager.get(0)).equals(new Vec2<ReadOnly>(-1500, 0)));
	}

	@Test
	public void test_distance() throws Exception
	{
		Assert.assertTrue(gridspace.distanceStatique(new PointDirige(pointManager.get(523), Direction.NE)) == 1414);
		Assert.assertTrue(gridspace.distanceStatique(new PointDirige(pointManager.get(523), Direction.N)) == 1000);
		Assert.assertTrue(gridspace.distanceStatique(new PointDirige(pointManager.get(1), Direction.E)) == Integer.MAX_VALUE);
	}

	@Test
	public void test_distanceHeuristique() throws Exception
	{
		Assert.assertTrue(pointManager.get(1).distanceHeuristiqueDStarLite(pointManager.get(2)) == 1000);
		Assert.assertTrue(pointManager.get(1).distanceHeuristiqueDStarLite(pointManager.get(65)) == 1000);
		Assert.assertTrue(pointManager.get(1).distanceHeuristiqueDStarLite(pointManager.get(64)) == 1414);
		Assert.assertTrue(pointManager.get(64).distanceHeuristiqueDStarLite(pointManager.get(1)) == 1414);
		Assert.assertTrue(pointManager.get(1).distanceHeuristiqueDStarLite(pointManager.get(1+2*64)) == 2000);
		Assert.assertTrue(pointManager.get(1+2*64).distanceHeuristiqueDStarLite(pointManager.get(1)) == 2000);
		Assert.assertTrue(pointManager.get(64).distanceHeuristiqueDStarLite(pointManager.get(63)) == 63414);
		Assert.assertTrue(pointManager.get(63).distanceHeuristiqueDStarLite(pointManager.get(64)) == 63414);
	}
	
	@Test
	public void test_getGridPointVoisin() throws Exception
	{
		for(int i = 0; i < 8; i++)
		{
			log.debug(i);
			Assert.assertEquals((i<4?1414:1000), pointManager.getGridPointVoisin(pointManager.get(150), Direction.values()[i]).distanceHeuristiqueDStarLite(pointManager.get(150)));
		}

		Assert.assertEquals(null, pointManager.getGridPointVoisin(pointManager.get(21), Direction.S));
		Assert.assertEquals(null, pointManager.getGridPointVoisin(pointManager.get(63), Direction.SE));
		Assert.assertEquals(null, pointManager.getGridPointVoisin(pointManager.get(127), Direction.E));
		Assert.assertEquals(null, pointManager.getGridPointVoisin(pointManager.get(128), Direction.O));
		Assert.assertEquals(null, pointManager.getGridPointVoisin(pointManager.get(21), Direction.SE));
	}
	
	@Test
	public void test_computeGridPoint() throws Exception
	{
		for(int i = 0; i < PointGridSpace.NB_POINTS; i++)
			Assert.assertTrue(pointManager.get(PointGridSpace.computeVec2(pointManager.get(i))).hashCode() == i);
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
