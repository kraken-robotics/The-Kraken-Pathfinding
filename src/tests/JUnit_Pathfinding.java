package tests;

import obstacles.ObstacleManager;

import org.junit.Before;

import pathfinding.Pathfinding;
import enums.ServiceNames;

public class JUnit_Pathfinding extends JUnit_Test {

	private Pathfinding pathfinding;
	private ObstacleManager obstaclemanager;
	
    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        pathfinding = (Pathfinding) container.getService(ServiceNames.PATHFINDING);
		obstaclemanager = (ObstacleManager) container.getService(ServiceNames.OBSTACLE_MANAGER);
    }

}
