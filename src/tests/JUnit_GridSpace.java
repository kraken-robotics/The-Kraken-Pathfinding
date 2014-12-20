package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pathfinding.GridSpace;
import smartMath.Vec2;
import utils.Config;
import enums.PathfindingNodes;
import enums.ServiceNames;

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
	public void test_nearestReachableNode() throws Exception
	{
		Assert.assertEquals(PathfindingNodes.BAS_DROITE, gridspace.nearestReachableNode(PathfindingNodes.BAS_DROITE.getCoordonnees().plusNewVector(new Vec2(10, -40)), Config.getDateDebutMatch()));
	}

	@Test
	public void test_traversable() throws Exception
	{
		config.setDateDebutMatch();
		Assert.assertTrue(!gridspace.isTraversable(PathfindingNodes.BAS_DROITE, PathfindingNodes.BAS_GAUCHE, Config.getDateDebutMatch()));
		gridspace.setAvoidGameElement(true);
		Assert.assertTrue(!gridspace.isTraversable(PathfindingNodes.BAS_DROITE, PathfindingNodes.DEVANT_DEPART_GAUCHE, Config.getDateDebutMatch()));
		gridspace.setAvoidGameElement(false);
		gridspace.reinitConnections();
		Assert.assertTrue(gridspace.isTraversable(PathfindingNodes.BAS_DROITE, PathfindingNodes.DEVANT_DEPART_GAUCHE, Config.getDateDebutMatch()));

		Assert.assertTrue(gridspace.isTraversable(PathfindingNodes.NODE_TAPIS, PathfindingNodes.BAS_GAUCHE, Config.getDateDebutMatch()));
		gridspace.creer_obstacle(new Vec2(-220, 830));
		// mise à jour du gridspace
		gridspace.reinitConnections();
		Assert.assertTrue(!gridspace.isTraversable(PathfindingNodes.NODE_TAPIS, PathfindingNodes.BAS_GAUCHE, Config.getDateDebutMatch()));
	}

    @Test
    public void test_symetrie() throws Exception
    {
		for(PathfindingNodes i : PathfindingNodes.values())
			for(PathfindingNodes j : PathfindingNodes.values())
				Assert.assertTrue(gridspace.isTraversable(i,j, Config.getDateDebutMatch()) == gridspace.isTraversable(j,i, Config.getDateDebutMatch()));
		gridspace.reinitConnections();
		for(PathfindingNodes i : PathfindingNodes.values())
			for(PathfindingNodes j : PathfindingNodes.values())
				Assert.assertTrue(gridspace.isTraversable(i,j, Config.getDateDebutMatch()) == gridspace.isTraversable(j,i, Config.getDateDebutMatch()));
    }

}
