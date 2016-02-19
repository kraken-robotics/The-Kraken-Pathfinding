package tests;

import java.util.BitSet;
import java.util.ListIterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pathfinding.dstarlite.GridSpace;
import permissions.ReadOnly;
import container.ServiceNames;
import utils.Vec2;

/**
 * Tests unitaires de GridSpace
 * @author pf
 *
 */

public class JUnit_GridSpace extends JUnit_Test {

	private GridSpace gridspace;
	
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
		Assert.assertTrue(gridspace.distanceDStarLite(1, 2) == 1414);
		Assert.assertTrue(gridspace.distanceDStarLite(1, 4) == 1000);
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
			Assert.assertEquals((i<4?1414:1000), GridSpace.distanceHeuristiqueDStarLite(GridSpace.getGridPointVoisin(150, i), 150));
		}
		Assert.assertEquals(-1, GridSpace.getGridPointVoisin(21, 5));
		Assert.assertEquals(-1, GridSpace.getGridPointVoisin(63, 1));
		Assert.assertEquals(-1, GridSpace.getGridPointVoisin(127, 7));
		Assert.assertEquals(-1, GridSpace.getGridPointVoisin(128, 6));
		Assert.assertEquals(-1, GridSpace.getGridPointVoisin(21, 1));
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
		ListIterator<Integer> b = gridspace.getWhatChanged();
		Assert.assertTrue(!b.hasNext());
		gridspace.addObstacle(new Vec2<ReadOnly>(200, 100), false);
		b = gridspace.getWhatChanged();
		Assert.assertTrue(b.hasNext());
		gridspace.addObstacle(new Vec2<ReadOnly>(400, 100), false);
		b = gridspace.getWhatChanged();
		Assert.assertTrue(b.hasNext());
	}

}
