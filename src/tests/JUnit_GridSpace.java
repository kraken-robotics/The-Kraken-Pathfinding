package tests;

import org.junit.Assert;

import obstacles.ObstacleManager;

import org.junit.Before;
import org.junit.Test;

import pathfinding.GridSpace;
import enums.PathfindingNodes;
import enums.ServiceNames;

/**
 * Tests unitaires de GridSpace
 * @author pf
 *
 */

public class JUnit_GridSpace extends JUnit_Test {

	private GridSpace gridspace;
	private ObstacleManager obstaclemanager;
	
	@Before
    public void setUp() throws Exception {
        super.setUp();
        gridspace = (GridSpace) container.getService(ServiceNames.GRID_SPACE);
		obstaclemanager = (ObstacleManager) container.getService(ServiceNames.OBSTACLE_MANAGER);
    }
    
/*	@Test
	public void test_traversable() throws Exception
	{
		Assert.assertTrue(!gridspace.isTraversable(PathfindingNodes.SCRIPT_PLOT_10, PathfindingNodes.SCRIPT_PLOT_9));
		Assert.assertTrue(!gridspace.isTraversable(PathfindingNodes.SCRIPT_PLOT_7, PathfindingNodes.SCRIPT_PLOT_9));
	}
	*/
	
    @Test
    public void test_symetrie() throws Exception
    {
		for(PathfindingNodes i : PathfindingNodes.values())
			for(PathfindingNodes j : PathfindingNodes.values())
				Assert.assertTrue(gridspace.isTraversable(i,j) == gridspace.isTraversable(j,i));
		gridspace.reinitConnections(System.currentTimeMillis());
		for(PathfindingNodes i : PathfindingNodes.values())
			for(PathfindingNodes j : PathfindingNodes.values())
				Assert.assertTrue(gridspace.isTraversable(i,j) == gridspace.isTraversable(j,i));
    }

}
