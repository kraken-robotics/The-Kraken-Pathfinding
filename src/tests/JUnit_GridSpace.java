package tests;

import obstacles.ObstaclesMobilesIterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pathfinding.GridSpace;
import permissions.ReadOnly;
import planification.astar.arc.PathfindingNodes;
import container.ServiceNames;
import utils.ConfigInfo;
import utils.Vec2;

/**
 * Tests unitaires de GridSpace
 * @author pf
 *
 */

public class JUnit_GridSpace extends JUnit_Test {

	private GridSpace gridspace;
	private ObstaclesMobilesIterator obstaclemanager;
	
	@Before
    public void setUp() throws Exception {
        super.setUp();
        gridspace = (GridSpace) container.getService(ServiceNames.GRID_SPACE);
        obstaclemanager = (ObstaclesMobilesIterator) container.getService(ServiceNames.OBSTACLE_MANAGER);
    }
   
	@Test
	public void test_nearestReachableNode() throws Exception
	{
		Assert.assertEquals(PathfindingNodes.BAS_DROITE, gridspace.nearestReachableNode(PathfindingNodes.BAS_DROITE.getCoordonnees().plusNewVector(new Vec2<ReadOnly>(10, -40)).getReadOnly(), 0));
	}

	@Test
	public void test_traversable() throws Exception
	{
		config.set(ConfigInfo.DATE_DEBUT_MATCH, System.currentTimeMillis());
		Assert.assertTrue(!gridspace.isTraversable(PathfindingNodes.BAS_DROITE, PathfindingNodes.BAS_GAUCHE, 0));
		gridspace.setAvoidGameElement(true);
		Assert.assertTrue(!gridspace.isTraversable(PathfindingNodes.BAS_DROITE, PathfindingNodes.DEVANT_DEPART_GAUCHE, 0));
		gridspace.setAvoidGameElement(false);
		gridspace.reinitConnections();
		Assert.assertTrue(gridspace.isTraversable(PathfindingNodes.BAS_DROITE, PathfindingNodes.DEVANT_DEPART_GAUCHE, 0));

		Assert.assertTrue(gridspace.isTraversable(PathfindingNodes.NODE_TAPIS, PathfindingNodes.BAS_GAUCHE, 0));
		obstaclemanager.creerObstacle(new Vec2<ReadOnly>(-220, 830), (int)(System.currentTimeMillis() - config.getLong(ConfigInfo.DATE_DEBUT_MATCH)));
		// mise à jour du gridspace
		gridspace.reinitConnections();
		Assert.assertTrue(!gridspace.isTraversable(PathfindingNodes.NODE_TAPIS, PathfindingNodes.BAS_GAUCHE, 0));
	}

    @Test
    public void test_symetrie() throws Exception
    {
		for(PathfindingNodes i : PathfindingNodes.values())
			for(PathfindingNodes j : PathfindingNodes.values())
				Assert.assertTrue(gridspace.isTraversable(i,j,0) == gridspace.isTraversable(j,i,0));
		gridspace.reinitConnections();
		for(PathfindingNodes i : PathfindingNodes.values())
			for(PathfindingNodes j : PathfindingNodes.values())
				Assert.assertTrue(gridspace.isTraversable(i,j,0) == gridspace.isTraversable(j,i,0));
    }

}
