package tests;

import org.junit.Assert;

import obstacles.ObstacleManager;

import org.junit.Before;
import org.junit.Test;

import pathfinding.GridSpace;
import smartMath.Vec2;
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
   
	@Test
	public void test_nearestReachableNode() throws Exception
	{
		Assert.assertEquals(PathfindingNodes.SCRIPT_CLAP_1, gridspace.nearestReachableNode(PathfindingNodes.SCRIPT_CLAP_1.getCoordonnees().plusNewVector(new Vec2(10, -40))));
		// du fait du cache, test_nearestReachableNode renverra toujours SCRIPT_CLAP_1 
		Assert.assertEquals(PathfindingNodes.SCRIPT_CLAP_1, gridspace.nearestReachableNode(PathfindingNodes.MILIEU_1.getCoordonnees().plusNewVector(new Vec2(30, -10))));
		gridspace.copy(gridspace, System.currentTimeMillis());
		// nearestReachableNode a été réinitialisé
		Assert.assertEquals(PathfindingNodes.MILIEU_1, gridspace.nearestReachableNode(PathfindingNodes.MILIEU_1.getCoordonnees().plusNewVector(new Vec2(30, -10))));
	}

	@Test
	public void test_iterator() throws Exception
	{
		boolean[] verification = new boolean[PathfindingNodes.values().length];
		for(PathfindingNodes j : PathfindingNodes.values())
		{
			gridspace.reinitIterator(j);
			for(PathfindingNodes i : PathfindingNodes.values())
				verification[i.ordinal()] = false;
			while(gridspace.hasNext())
			{
				Assert.assertTrue(verification[gridspace.next().ordinal()] == false);
				verification[gridspace.next().ordinal()] = true;
			}
			for(PathfindingNodes i : PathfindingNodes.values())
				Assert.assertTrue((gridspace.isTraversable(i, j) && i != j) == verification[i.ordinal()]);
		}
	}
	
	@Test
	public void test_traversable() throws Exception
	{
		Assert.assertTrue(!gridspace.isTraversable(PathfindingNodes.SCRIPT_PLOT_10, PathfindingNodes.SCRIPT_PLOT_9));
		Assert.assertTrue(!gridspace.isTraversable(PathfindingNodes.SCRIPT_PLOT_7, PathfindingNodes.SCRIPT_PLOT_9));
		Assert.assertTrue(gridspace.isTraversable(PathfindingNodes.SCRIPT_PLOT_3, PathfindingNodes.SCRIPT_PLOT_1));
		obstaclemanager.creer_obstacle(new Vec2(0, 600));
		// mise à jour du gridspace
		gridspace.reinitConnections(System.currentTimeMillis());
		Assert.assertTrue(!gridspace.isTraversable(PathfindingNodes.SCRIPT_PLOT_3, PathfindingNodes.SCRIPT_PLOT_1));
	}
	
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
