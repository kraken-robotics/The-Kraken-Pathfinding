package tests;

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
	private GridSpace gridspace;
	
	@Before
    public void setUp() throws Exception {
        super.setUp();
        pathfinding = (Pathfinding) container.getService(ServiceNames.PATHFINDING);
		gridspace = (GridSpace) container.getService(ServiceNames.GRID_SPACE);
    }

	@Test(expected=PathfindingRobotInObstacleException.class)
    public void test_robot_dans_obstacle() throws Exception
    {
    	gridspace.creer_obstacle(new Vec2(100, 100));
    	pathfinding.computePath(new Vec2(80, 80), PathfindingNodes.COIN_1, gridspace);
    }

	@Test(expected=PathfindingException.class)
    public void test_obstacle() throws Exception
    {
    	gridspace.creer_obstacle(PathfindingNodes.SCRIPT_PLOT_7.getCoordonnees());
    	pathfinding.computePath(new Vec2(80, 80), PathfindingNodes.SCRIPT_PLOT_7, gridspace);
    }

}
