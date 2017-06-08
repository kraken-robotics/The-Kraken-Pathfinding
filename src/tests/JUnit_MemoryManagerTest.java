package tests;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import robot.RobotChrono;
import robot.RobotVrai;
import smartMath.Vec2;
import strategie.GameState;
import strategie.MemoryManager;
import table.Table;

/**
 * Tests unitaires du memory manager
 * @author pf
 *
 */

public class JUnit_MemoryManagerTest extends JUnit_Test {

	private GameState<RobotVrai> state;
	private MemoryManager memorymanager;
	
	@SuppressWarnings("unchecked")
    @Before
	public void setUp() throws Exception {
		super.setUp();
		log.debug("JUnit_MemoryManagerTest.setUp()", this);
		state = (GameState<RobotVrai>)container.getService("RealGameState");
		memorymanager = (MemoryManager)container.getService("MemoryManager");
	}

	@Test
	public void test_clone_1etage() throws Exception
	{
		log.debug("JUnit_MemoryManagerTest.test_cloneTable_1etage()", this);
		Table cloned = memorymanager.getClone(0).table;
		System.out.println(state.table.hashCode());
        System.out.println(cloned.hashCode());
		Assert.assertTrue(state.table.equals(cloned));
	}

   @Test
    public void test_clone_beaucoup() throws Exception
    {
        log.debug("JUnit_MemoryManagerTest.test_cloneTable_1etage()", this);
        Table cloned = memorymanager.getClone(1000).table;
        Assert.assertTrue(state.table.equals(cloned));
    }

	@Test
	public void test_clone_1etage_modification() throws Exception
	{
		log.debug("JUnit_MemoryManagerTest.test_cloneTable_1etage_modification()", this);
        Table cloned = memorymanager.getClone(0).table;
		state.table.creer_obstacle(new Vec2(0,1000));
		state.table.pickFire(0);
		state.table.pickTree(0);
		Assert.assertTrue(!state.table.equals(cloned));
	}

	@Test
	public void test_clone_2etages() throws Exception
	{
		log.debug("JUnit_MemoryManagerTest.test_cloneTable_2etages()", this);
		Table cloned = memorymanager.getClone(0).table;
		cloned.creer_obstacle(new Vec2(0,1000));
		Assert.assertTrue(!state.table.equals(cloned));
		Table cloned2 = memorymanager.getClone(1).table;
		Assert.assertTrue(!cloned2.equals(state.table));
		Assert.assertTrue(cloned2.equals(cloned));
	}

	@Test
	public void test_clone_encore_un_test() throws Exception
	{
		log.debug("JUnit_MemoryManagerTest.test_cloneTable_encore_un_test()", this);
		Table cloned = memorymanager.getClone(0).table;
		cloned.creer_obstacle(new Vec2(0,1000));
		Assert.assertTrue(!state.table.equals(cloned));
		Table cloned2 = memorymanager.getClone(0).table;
		Assert.assertTrue(cloned2.equals(state.table));
		// L'assertion suivante devrait en logique être fausse.
		// Le fait est qu'une telle comparaison (deux éléments d'un même niveau) ne sera jamais effectuée
		Assert.assertTrue(cloned2.equals(cloned));
	}

    @Test
    public void test_clone_pathfinding() throws Exception
    {
        log.debug("JUnit_MemoryManagerTest.test_clone_pathfinding()", this);
        GameState<RobotChrono> cloned = memorymanager.getClone(0);
        Assert.assertTrue(state.table.equals(cloned.table));
        Assert.assertTrue(state.pathfinding.equals(cloned.pathfinding));
        state.table.creer_obstacle(new Vec2(0,1000));
        state.table.pickFire(0);
        state.table.pickTree(0);
        Assert.assertTrue(!state.table.equals(cloned.table));
    }

}
