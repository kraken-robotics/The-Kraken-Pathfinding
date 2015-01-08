package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import container.ServiceNames;
import astar.MemoryManager;
import astar.arc.PathfindingNodes;
import robot.RobotChrono;
import strategie.GameState;
import utils.ConfigInfo;

/**
 * Tests unitaires du memorymanager
 * @author pf
 *
 */

public class JUnit_MemoryManager extends JUnit_Test {

	private MemoryManager memorymanager;
	
	@Before
    public void setUp() throws Exception {
        super.setUp();
    	config.set(ConfigInfo.DUREE_PEREMPTION_OBSTACLES, 100);
		memorymanager = (MemoryManager) container.getService(ServiceNames.MEMORY_MANAGER);
	}

	@Test
    public void test_echange() throws Exception
    {
		Assert.assertTrue(memorymanager.isMemoryManagerEmpty(0));
		GameState<RobotChrono> state_chrono1 = memorymanager.getNewGameState(0);
		state_chrono1.robot.setPositionPathfinding(PathfindingNodes.BAS);
		Assert.assertTrue(!memorymanager.isMemoryManagerEmpty(0));
		GameState<RobotChrono> state_chrono2 = memorymanager.getNewGameState(0);
		state_chrono2.robot.setPositionPathfinding(PathfindingNodes.BAS_DROITE);
		memorymanager.destroyGameState(state_chrono1, 0);
		state_chrono1 = null;
		// on vérifie que l'échange est bien fait
		Assert.assertEquals(state_chrono2.robot.getPositionPathfinding(), PathfindingNodes.BAS_DROITE);
		Assert.assertTrue(!memorymanager.isMemoryManagerEmpty(0));
		memorymanager.destroyGameState(state_chrono2, 0);
		state_chrono2 = null;
		Assert.assertTrue(memorymanager.isMemoryManagerEmpty(0));
    }
}
