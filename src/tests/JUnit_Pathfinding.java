package tests;

import java.util.ArrayList;

import org.junit.Assert;
import obstacles.ObstacleManager;

import org.junit.Before;
import org.junit.Test;

import pathfinding.GridSpace;
import pathfinding.Pathfinding;
import smartMath.Vec2;
import enums.PathfindingNodes;
import enums.ServiceNames;
import exceptions.PathfindingException;
import exceptions.PathfindingRobotInObstacleException;

/**
 * Tests unitaires de la recherche de chemin.
 * @author pf
 *
 */

// TODO: ajouter des tests avec des vérifications de chemin

public class JUnit_Pathfinding extends JUnit_Test {

	private Pathfinding pathfinding;
	private ObstacleManager obstaclemanager;
	private GridSpace gridspace;
	
	@Before
    public void setUp() throws Exception {
        super.setUp();
        pathfinding = (Pathfinding) container.getService(ServiceNames.PATHFINDING);
		obstaclemanager = (ObstacleManager) container.getService(ServiceNames.OBSTACLE_MANAGER);
		gridspace = (GridSpace) container.getService(ServiceNames.GRID_SPACE);
    }

	@Test(expected=PathfindingRobotInObstacleException.class)
    public void test_robot_dans_obstacle() throws Exception
    {
    	obstaclemanager.creer_obstacle(new Vec2(100, 100));
    	pathfinding.computePath(new Vec2(80, 80), PathfindingNodes.COIN_1, gridspace);
    }

	@Test(expected=PathfindingException.class)
    public void test_obstacle() throws Exception
    {
    	obstaclemanager.creer_obstacle(PathfindingNodes.SCRIPT_PLOT_7.getCoordonnees());
    	pathfinding.computePath(new Vec2(80, 80), PathfindingNodes.SCRIPT_PLOT_7, gridspace);
    }

	@Test
    public void test_chemin1() throws Exception
    {
    	ArrayList<PathfindingNodes> chemin =  pathfinding.computePath(PathfindingNodes.COIN_2.getCoordonnees().plusNewVector(new Vec2(20, 30)), PathfindingNodes.COIN_1, gridspace);
    	Assert.assertTrue(chemin.size() == 2);
    	Assert.assertTrue(chemin.get(0) == PathfindingNodes.COIN_2);
    	Assert.assertTrue(chemin.get(1) == PathfindingNodes.COIN_1);
    }

	@Test
    public void test_chemin() throws Exception
    {
    	ArrayList<PathfindingNodes> chemin =  pathfinding.computePath(PathfindingNodes.SCRIPT_PLOT_9.getCoordonnees().plusNewVector(new Vec2(20, 30)), PathfindingNodes.SCRIPT_PLOT_10, gridspace);
    	for(PathfindingNodes n: chemin)
    		System.out.println(n+": "+n.getCoordonnees());
//    	Assert.assertTrue(chemin.size() == 2);
//    	Assert.assertTrue(chemin.get(0) == PathfindingNodes.COIN_2);
//    	Assert.assertTrue(chemin.get(1) == PathfindingNodes.COIN_1);
    }

}
