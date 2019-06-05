/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pfg.kraken.dstarlite.navmesh.Navmesh;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.struct.XY_RW;

/**
 * Tests unitaires du Navmesh
 * 
 * @author pf
 *
 */

public class Test_Navmesh extends JUnit_Test
{
	private Navmesh navmesh;
	
	@Before
	public void setUp() throws Exception
	{}

	@Test
	public void test_empty() throws Exception
	{
		super.setUpWith(null, "default", "empty");
		navmesh = injector.getService(Navmesh.class);
		Assert.assertNotEquals(0, navmesh.mesh.edges.length);
		Assert.assertNotEquals(0, navmesh.mesh.nodes.length);
		Assert.assertNotEquals(0, navmesh.mesh.triangles.length);
	}
	
	@Test
	public void test_simple() throws Exception
	{
		List<Obstacle> obs = new ArrayList<Obstacle>();
		obs.add(new RectangularObstacle(new XY_RW(0,1000), 2000, 2000));
		super.setUpWith(obs, "default", "graphic", "navmesh");
		navmesh = injector.getService(Navmesh.class);
	}
}
