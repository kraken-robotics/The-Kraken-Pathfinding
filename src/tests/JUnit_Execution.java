package tests;

import org.junit.Test;

import astar.arc.PathfindingNodes;
import robot.RobotReal;
import strategie.Execution;
import utils.Config;
import container.ServiceNames;

/**
 * Tests unitaires de l'exécution de scripts
 * @author pf
 *
 */

public class JUnit_Execution extends JUnit_Test {

	private Execution execution;
	
    public void setUp() throws Exception {
        super.setUp();
        container.startAllThreads();
        RobotReal robot = (RobotReal) container.getService(ServiceNames.ROBOT_REAL);
        robot.setPosition(PathfindingNodes.POINT_DEPART.getCoordonnees());
        robot.setOrientation(Math.PI);
        execution = (Execution) container.getService(ServiceNames.EXECUTION);
    }
    
	@Test
	public void test_boucle_execution() throws Exception
	{
		Config.matchDemarre = true;
		execution.boucleExecution();
	}
	
}
