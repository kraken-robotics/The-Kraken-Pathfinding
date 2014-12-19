package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import robot.RobotChrono;
import strategie.GameState;
import strategie.MemoryManager;
import enums.ConfigInfo;
import enums.PathfindingNodes;
import enums.ServiceNames;

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
		Assert.assertTrue(memorymanager.isMemoryManagerEmpty());
		GameState<RobotChrono> state_chrono1 = memorymanager.getNewGameState();
		state_chrono1.robot.setPositionPathfinding(PathfindingNodes.BAS);
		Assert.assertTrue(!memorymanager.isMemoryManagerEmpty());
		GameState<RobotChrono> state_chrono2 = memorymanager.getNewGameState();
		state_chrono2.robot.setPositionPathfinding(PathfindingNodes.BAS_DROITE);
		state_chrono1 = memorymanager.destroyGameState(state_chrono1);
		// on vérifie que l'échange est bien fait
		Assert.assertEquals(state_chrono2.robot.getPositionPathfinding(), PathfindingNodes.BAS_DROITE);
		Assert.assertTrue(!memorymanager.isMemoryManagerEmpty());
		state_chrono2 = memorymanager.destroyGameState(state_chrono2);
		Assert.assertTrue(memorymanager.isMemoryManagerEmpty());
    }

	@Test
    public void test_stress() throws Exception
    {
		for(int i = 0; i < 1000; i++)
			memorymanager.getNewGameState();
    }
}
