/*
Copyright (C) 2016 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package tests;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import graphic.PrintBuffer;
import pathfinding.dstarlite.gridspace.Direction;
import pathfinding.dstarlite.gridspace.GridSpace;
import pathfinding.dstarlite.gridspace.Masque;
import pathfinding.dstarlite.gridspace.MasqueManager;
import pathfinding.dstarlite.gridspace.PointDirige;
import pathfinding.dstarlite.gridspace.PointDirigeManager;
import pathfinding.dstarlite.gridspace.PointGridSpace;
import pathfinding.dstarlite.gridspace.PointGridSpaceManager;
import utils.ConfigInfo;
import utils.Vec2RO;

/**
 * Tests unitaires de GridSpace
 * @author pf
 *
 */

public class JUnit_GridSpace extends JUnit_Test {

	private GridSpace gridspace;
	private PointGridSpaceManager pointManager;
	private PointDirigeManager pointDManager;
	
	@Override
	@Before
    public void setUp() throws Exception {
        super.setUp();
        gridspace = container.getService(GridSpace.class);
        pointManager = container.getService(PointGridSpaceManager.class);
        pointDManager = container.getService(PointDirigeManager.class);
       
    }
	
	@Test
	public void test_computeVec2() throws Exception
	{
		log.debug(pointManager.get(0).computeVec2());
		log.debug(pointManager.get(64).computeVec2());
		log.debug(pointManager.get(63).computeVec2());
		Assert.assertTrue(pointManager.get(0).computeVec2().equals(new Vec2RO(-1500, 0)));
	}

	@Test
	public void test_distance() throws Exception
	{
		Assert.assertTrue(gridspace.distanceStatique(pointDManager.get(pointManager.get(523), Direction.NE)) == 1414);
		Assert.assertTrue(gridspace.distanceStatique(pointDManager.get(pointManager.get(523), Direction.N)) == 1000);
		Assert.assertTrue(gridspace.distanceStatique(pointDManager.get(pointManager.get(1), Direction.E)) == Integer.MAX_VALUE);
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
			Assert.assertTrue(pointManager.get(pointManager.get(i).computeVec2()).hashCode() == i);
	}
	
	@Test
	public void test_ajout_obstacle() throws Exception
	{
    	int peremption = config.getInt(ConfigInfo.DUREE_PEREMPTION_OBSTACLES);
		Assert.assertTrue(gridspace.getCurrentObstacles().isEmpty());
		ArrayList<ArrayList<PointDirige>> b = gridspace.getOldAndNewObstacles();
//		log.debug(b.get(0).size()+" "+b.get(1).size());
		Assert.assertTrue(b.get(0).isEmpty());
		Assert.assertTrue(b.get(1).isEmpty());
		gridspace.addObstacleAndRemoveNearbyObstacles(new Vec2RO(200, 1100));
		b = gridspace.getOldAndNewObstacles();
//		log.debug(b.get(0).size()+" "+b.get(1).size());
		Assert.assertTrue(b.get(0).isEmpty());
		Assert.assertTrue(!b.get(1).isEmpty());
		gridspace.addObstacleAndRemoveNearbyObstacles(new Vec2RO(210, 1100));
		b = gridspace.getOldAndNewObstacles();
//		log.debug(b.get(0).size()+" "+b.get(1).size());
		Assert.assertTrue(!b.get(0).isEmpty()); // on a supprimé l'autre qui était trop proche
		Assert.assertTrue(!b.get(1).isEmpty());
		gridspace.addObstacleAndRemoveNearbyObstacles(new Vec2RO(400, 600));
		b = gridspace.getOldAndNewObstacles();
//		log.debug(b.get(0).size()+" "+b.get(1).size());
		Assert.assertTrue(b.get(0).isEmpty());
		Assert.assertTrue(!b.get(1).isEmpty());
		b = gridspace.getOldAndNewObstacles();
//		log.debug(b.get(0).size()+" "+b.get(1).size());
		Assert.assertTrue(b.get(0).isEmpty());
		Assert.assertTrue(b.get(1).isEmpty());
		Thread.sleep(peremption+10);
		b = gridspace.getOldAndNewObstacles();
//		log.debug(b.get(0).size()+" "+b.get(1).size());
		Assert.assertTrue(!b.get(0).isEmpty());
		Assert.assertTrue(b.get(1).isEmpty());
		gridspace.addObstacleAndRemoveNearbyObstacles(new Vec2RO(400, 800));
		Assert.assertTrue(!gridspace.getCurrentObstacles().isEmpty());
		Assert.assertTrue(!gridspace.getCurrentObstacles().isEmpty());
		b = gridspace.getOldAndNewObstacles();
//		log.debug(b.get(0).size()+" "+b.get(1).size());
		Assert.assertTrue(b.get(0).isEmpty());
		Assert.assertTrue(b.get(1).isEmpty());
	}

	@Test
	public void test_masque() throws Exception
	{
		MasqueManager mm = container.getService(MasqueManager.class);
		PrintBuffer buffer = container.getService(PrintBuffer.class);
		Masque m = mm.getMasque(new Vec2RO(0, 1000));
		
		if(config.getBoolean(ConfigInfo.GRAPHIC_D_STAR_LITE))
			buffer.add(m);
	}
	
}
