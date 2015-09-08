package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pathfinding.thetastar.MemoryManager;
import permissions.ReadWrite;
import container.ServiceNames;
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
/*		Assert.assertTrue(memorymanager.isMemoryManagerEmpty());
		GameState<RobotChrono,ReadWrite> state_chrono1 = memorymanager.getNewNode();
		GameState.setPositionPathfinding(state_chrono1, PathfindingNodes.BAS);
		Assert.assertTrue(!memorymanager.isMemoryManagerEmpty());
		GameState<RobotChrono,ReadWrite> state_chrono2 = memorymanager.getNewNode();
		GameState.setPositionPathfinding(state_chrono2, PathfindingNodes.BAS_DROITE);
		memorymanager.destroyNode(state_chrono1);
		state_chrono1 = null;
		// on vérifie que l'échange est bien fait
		Assert.assertEquals(GameState.getPositionPathfinding(state_chrono2.getReadOnly()), PathfindingNodes.BAS_DROITE);
		Assert.assertTrue(!memorymanager.isMemoryManagerEmpty());
		memorymanager.destroyNode(state_chrono2);
		state_chrono2 = null;
		Assert.assertTrue(memorymanager.isMemoryManagerEmpty());*/
    }
}
