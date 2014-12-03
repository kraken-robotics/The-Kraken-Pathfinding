package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import robot.RobotChrono;
import robot.RobotVrai;
import strategie.GameState;
import strategie.MemoryManager;
import table.Table;
import enums.ServiceNames;

public class JUnit_MemoryManager extends JUnit_Test {

	private GameState<RobotVrai> state;
	private MemoryManager memorymanager;
	
    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        memorymanager = (MemoryManager) container.getService(ServiceNames.MEMORY_MANAGER);
		state = (GameState<RobotVrai>)container.getService(ServiceNames.REAL_GAME_STATE);
    }
    
	@Test
	public void test_clone_1etage() throws Exception
	{
		Table cloned = memorymanager.getClone(0).table;
		Assert.assertTrue(state.table.equals(cloned));
	}

   @Test
    public void test_clone_beaucoup() throws Exception
    {
        Table cloned = memorymanager.getClone(1000).table;
        Assert.assertTrue(state.table.equals(cloned));
    }

	@Test
	public void test_clone_1etage_modification() throws Exception
	{
        Table cloned = memorymanager.getClone(0).table;
        cloned.setClapDone(0);
        Assert.assertTrue(!state.table.equals(cloned));
	}

	@Test
	public void test_clone_2etages() throws Exception
	{
		Table cloned = memorymanager.getClone(0).table;
        cloned.setClapDone(0);
		Assert.assertTrue(!state.table.equals(cloned));
		Table cloned2 = memorymanager.getClone(1).table;
		Assert.assertTrue(!cloned2.equals(state.table));
		Assert.assertTrue(cloned2.equals(cloned));
	}

	@Test
	public void test_clone_encore_un_test() throws Exception
	{
		Table cloned = memorymanager.getClone(0).table;
        cloned.setClapDone(0);
		Assert.assertTrue(!state.table.equals(cloned));
		Table cloned2 = memorymanager.getClone(0).table;
		Assert.assertTrue(cloned2.equals(state.table));
		// L'assertion suivante devrait en logique être fausse.
		// Le fait est qu'une telle comparaison (deux éléments d'un même niveau) ne sera jamais effectuée
		Assert.assertTrue(cloned2.equals(cloned));
	}

	@Test
	public void test_temps() throws Exception
	{
		GameState<RobotChrono> clone0 = memorymanager.getClone(0);
		Assert.assertEquals(0, clone0.temps_depuis_racine);
		long date_initiale = clone0.temps_depuis_debut;
		clone0.robot.sleep(2000);
		GameState<RobotChrono> clone1 = memorymanager.getClone(1);
		Assert.assertEquals(2000, clone1.temps_depuis_racine);
		Assert.assertEquals(2000+date_initiale, clone1.temps_depuis_debut);
		GameState<RobotChrono> clone2 = memorymanager.getClone(2);
		Assert.assertEquals(2000, clone2.temps_depuis_racine);
		Assert.assertEquals(2000+date_initiale, clone2.temps_depuis_debut);
	}	

	@Test
	public void test_points() throws Exception
	{
		GameState<RobotChrono> clone0 = memorymanager.getClone(0);
		Assert.assertEquals(0, clone0.robot.getPointsObtenus());
		clone0.robot.clapTombe();
		GameState<RobotChrono> clone1 = memorymanager.getClone(1);
		Assert.assertEquals(5, clone1.robot.getPointsObtenus());
		GameState<RobotChrono> clone2 = memorymanager.getClone(2);
		Assert.assertEquals(5, clone2.robot.getPointsObtenus());
	}
}
