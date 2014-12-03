package tests;

import org.junit.Assert;
import obstacles.ObstacleManager;

import org.junit.Before;
import org.junit.Test;

import smartMath.Vec2;
import enums.ServiceNames;

// TODO
// création d'obstacle
// péremption
// collision
// ...


public class JUnit_ObstacleManager extends JUnit_Test {

	private ObstacleManager obstaclemanager;
	
    @Before
    public void setUp() throws Exception {
        super.setUp();
        obstaclemanager = (ObstacleManager) container.getService(ServiceNames.OBSTACLE_MANAGER);
    }

    @Test
    public void test_creation() throws Exception
    {
    	Assert.assertTrue(obstaclemanager.nbObstaclesMobiles() == 0);
    	obstaclemanager.creer_obstacle(new Vec2(500, 500));
    	Assert.assertTrue(obstaclemanager.nbObstaclesMobiles() == 1);
    	obstaclemanager.creer_obstacle(new Vec2(-20, 10));
    	Assert.assertTrue(obstaclemanager.nbObstaclesMobiles() == 2);
    	obstaclemanager.creer_obstacle(new Vec2(5000, 200));
    	Assert.assertTrue(obstaclemanager.nbObstaclesMobiles() == 3);
    }

    // DEPENDS_ON_RULES
    @Test
    public void test_present() throws Exception
    {
    	Assert.assertTrue(!obstaclemanager.is_obstacle_fixe_present(new Vec2(500, 500), 1));
    	Assert.assertTrue(obstaclemanager.is_obstacle_fixe_present(new Vec2(30, 30), 1));
    	
    	// Vérification de la présence d'obstacles mobiles
    	Assert.assertTrue(!obstaclemanager.is_obstacle_mobile_present(new Vec2(500, 500), 1));
    	Assert.assertTrue(!obstaclemanager.is_obstacle_mobile_present(new Vec2(520, 520), 100));
    	obstaclemanager.creer_obstacle(new Vec2(500, 500));
    	Assert.assertTrue(obstaclemanager.is_obstacle_mobile_present(new Vec2(500, 500), 1));
    	Assert.assertTrue(obstaclemanager.is_obstacle_mobile_present(new Vec2(520, 520), 100));
    }
    
    @Test
    public void test_peremption() throws Exception
    {
    	config.set("duree_peremption_obstacles", 200); // 200 ms de péremption
    	obstaclemanager.updateConfig();
    	
    	obstaclemanager.creer_obstacle(new Vec2(500, 500));
    	Assert.assertTrue(obstaclemanager.is_obstacle_mobile_present(new Vec2(500, 500), 1));
    	obstaclemanager.supprimerObstaclesPerimes(System.currentTimeMillis()+100); // pas encore périmé
    	Assert.assertTrue(obstaclemanager.is_obstacle_mobile_present(new Vec2(500, 500), 1));
    	obstaclemanager.supprimerObstaclesPerimes(System.currentTimeMillis()+300); // ce sera périmé à cette date 
    	Assert.assertTrue(!obstaclemanager.is_obstacle_mobile_present(new Vec2(500, 500), 1));
    }
    
    // DEPENDS_ON_RULES
    @Test
    public void test_collision_obstacle_fixe() throws Exception
    {
    	Assert.assertTrue(obstaclemanager.obstacle_fixe_dans_segment(new Vec2(-1000, 30), new Vec2(1000, 30)));
    	Assert.assertTrue(!obstaclemanager.obstacle_fixe_dans_segment(new Vec2(500, 500), new Vec2(800, 800)));
    }

    @Test
    public void test_collision_obstacle_mobile() throws Exception
    {
    	Assert.assertTrue(!obstaclemanager.obstacle_proximite_dans_segment(new Vec2(100, 100), new Vec2(900, 900)));
    	obstaclemanager.creer_obstacle(new Vec2(-200, 500));    	
    	Assert.assertTrue(!obstaclemanager.obstacle_proximite_dans_segment(new Vec2(100, 100), new Vec2(900, 900)));
    	obstaclemanager.creer_obstacle(new Vec2(1200, 1200));    	
    	Assert.assertTrue(!obstaclemanager.obstacle_proximite_dans_segment(new Vec2(100, 100), new Vec2(900, 900)));
    	obstaclemanager.creer_obstacle(new Vec2(400, 500));    	
    	Assert.assertTrue(obstaclemanager.obstacle_proximite_dans_segment(new Vec2(100, 100), new Vec2(900, 900)));
    }

}
