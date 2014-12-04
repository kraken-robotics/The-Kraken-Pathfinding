package tests;

import obstacles.ObstacleManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import robot.RobotChrono;
import robot.RobotVrai;
import smartMath.Vec2;
import strategie.GameState;
import strategie.MemoryManager;
import table.Table;
import enums.ServiceNames;

public class JUnit_MemoryManager extends JUnit_Test {

	private GameState<RobotVrai> state;
	private MemoryManager memorymanager;
	private ObstacleManager obstaclemanager;
	
    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        memorymanager = (MemoryManager) container.getService(ServiceNames.MEMORY_MANAGER);
		state = (GameState<RobotVrai>)container.getService(ServiceNames.REAL_GAME_STATE);
		obstaclemanager = (ObstacleManager) container.getService(ServiceNames.OBSTACLE_MANAGER);
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
		for(int i = 0; i < 2; i++)
		{
			GameState<RobotChrono> clone0 = memorymanager.getClone(0);
			log.debug("AAA", this);
			Assert.assertEquals(0, clone0.getTempsDepuisRacine());
			long date_initiale = clone0.getTempsDepuisDebut();
			clone0.robot.sleep(2000);
			log.debug("BBB", this);
			GameState<RobotChrono> clone1 = memorymanager.getClone(1);
			log.debug("CCC", this);
			Assert.assertEquals(2000, clone1.getTempsDepuisRacine());
			Assert.assertEquals(2000+date_initiale, clone1.getTempsDepuisDebut());
			GameState<RobotChrono> clone2 = memorymanager.getClone(2);
			Assert.assertEquals(2000, clone2.getTempsDepuisRacine());
			Assert.assertEquals(2000+date_initiale, clone2.getTempsDepuisDebut());
		}
	}	

	@Test
	public void test_points() throws Exception
	{
		for(int i = 0; i < 2; i++)
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
	
	@Test
	public void test_suppression_obstacle() throws Exception
	{
		// il faut faire les tests en double. Une fois en clone, une fois un copy.
		for(int i = 0; i < 2; i++)
		{
	    	config.set("duree_peremption_obstacles", 200); // 200 ms de péremption
	    	obstaclemanager.updateConfig();
	    	obstaclemanager.creer_obstacle(new Vec2(500, 500));
			GameState<RobotChrono> clone0 = memorymanager.getClone(0);
			Assert.assertEquals(1,clone0.gridspace.nbObstaclesMobiles());
			clone0.robot.sleep(300);
			GameState<RobotChrono> clone1 = memorymanager.getClone(1);
			Assert.assertEquals(0,clone1.gridspace.nbObstaclesMobiles());
		}
	}
	
}
