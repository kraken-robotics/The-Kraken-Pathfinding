package tests;

import org.junit.Assert;

import obstacles.ObstacleManager;

import org.junit.Before;
import org.junit.Test;

import smartMath.Vec2;
import table.Table;
import enums.ConfigInfo;
import enums.PathfindingNodes;
import enums.ServiceNames;
import enums.Tribool;

/**
 * Tests unitaires pour ObstacleManager
 * @author pf
 *
 */

public class JUnit_ObstacleManager extends JUnit_Test {

	private ObstacleManager obstaclemanager;
	private Table table;
	
    @Before
    public void setUp() throws Exception {
        super.setUp();
        obstaclemanager = (ObstacleManager) container.getService(ServiceNames.OBSTACLE_MANAGER);
        table = (Table) container.getService(ServiceNames.TABLE);
    }

    @Test
    public void test_creation() throws Exception
    {
    	Assert.assertTrue(obstaclemanager.nbObstaclesMobiles() == 0);
    	obstaclemanager.creer_obstacle(new Vec2(500, 500), 0);
    	Assert.assertTrue(obstaclemanager.nbObstaclesMobiles() == 1);
    	obstaclemanager.creer_obstacle(new Vec2(-20, 10), 0);
    	Assert.assertTrue(obstaclemanager.nbObstaclesMobiles() == 2);
    	obstaclemanager.creer_obstacle(new Vec2(5000, 200), 0);
    	Assert.assertTrue(obstaclemanager.nbObstaclesMobiles() == 3);
    }

    // DEPENDS_ON_RULES
    @Test
    public void test_present() throws Exception
    {
    	Assert.assertTrue(!obstaclemanager.is_obstacle_fixe_present_capteurs(new Vec2(500, 500)));
    	Assert.assertTrue(obstaclemanager.is_obstacle_fixe_present_capteurs(new Vec2(30, 30)));
    	
    	// Vérification de la présence d'obstacles mobiles
    	Assert.assertTrue(!obstaclemanager.is_obstacle_mobile_present(new Vec2(500, 500), 1));
    	Assert.assertTrue(!obstaclemanager.is_obstacle_mobile_present(new Vec2(520, 520), 100));
    	obstaclemanager.creer_obstacle(new Vec2(500, 500), 0);
    	Assert.assertTrue(obstaclemanager.is_obstacle_mobile_present(new Vec2(500, 500), 1));
    	Assert.assertTrue(obstaclemanager.is_obstacle_mobile_present(new Vec2(520, 520), 100));
    }
    
    @Test
    public void test_peremption() throws Exception
    {
    	config.set(ConfigInfo.DUREE_PEREMPTION_OBSTACLES, 200); // 200 ms de péremption
    	obstaclemanager.updateConfig();
    	
    	obstaclemanager.creer_obstacle(new Vec2(0, 0), 0);
    	obstaclemanager.creer_obstacle(new Vec2(500, 500), 0);
    	obstaclemanager.creer_obstacle(new Vec2(0, 0), 0);
    	Assert.assertTrue(obstaclemanager.is_obstacle_mobile_present(new Vec2(500, 500), 1));
    	obstaclemanager.supprimerObstaclesPerimes(100); // pas encore périmé
    	Assert.assertTrue(obstaclemanager.is_obstacle_mobile_present(new Vec2(500, 500), 1));
    	obstaclemanager.supprimerObstaclesPerimes(300); // ce sera périmé à cette date 
    	Assert.assertTrue(!obstaclemanager.is_obstacle_mobile_present(new Vec2(500, 500), 1));
    }
    
    // DEPENDS_ON_RULES
    @Test
    public void test_collision_obstacle_fixe() throws Exception
    {
    	Assert.assertTrue(obstaclemanager.obstacle_fixe_dans_segment_pathfinding(new Vec2(-1000, 30), new Vec2(1000, 30)));
    	Assert.assertTrue(!obstaclemanager.obstacle_fixe_dans_segment_pathfinding(new Vec2(500, 500), new Vec2(800, 800)));
    }

    @Test
    public void test_collision_obstacle_mobile() throws Exception
    {
    	config.setDateDebutMatch();
    	Assert.assertTrue(!obstaclemanager.obstacle_proximite_dans_segment(new Vec2(100, 100), new Vec2(900, 900), 0));
    	obstaclemanager.creer_obstacle(new Vec2(-300, 500), 0);
    	Assert.assertTrue(!obstaclemanager.obstacle_proximite_dans_segment(new Vec2(100, 100), new Vec2(900, 900), 0));
    	obstaclemanager.creer_obstacle(new Vec2(1300, 1300), 0);
    	Assert.assertTrue(!obstaclemanager.obstacle_proximite_dans_segment(new Vec2(100, 100), new Vec2(900, 900), 0));
    	obstaclemanager.creer_obstacle(new Vec2(400, 500), 0);
    }
    @Test
    public void test_peremption_en_mouvement() throws Exception
    {
    	config.set(ConfigInfo.DUREE_PEREMPTION_OBSTACLES, 500); // 200 ms de péremption
    	obstaclemanager.updateConfig();
    	obstaclemanager.creer_obstacle(new Vec2(1200, 900), 0);
    	// Le temps qu'on y arrive, il devrait être périmé
    	Assert.assertTrue(!obstaclemanager.obstacle_proximite_dans_segment(new Vec2(-1200, 100), new Vec2(1200, 900), 0));
    }

    @Test
    public void test_collision_element_jeu() throws Exception
    {
    	Assert.assertTrue(obstaclemanager.obstacle_table_dans_segment(PathfindingNodes.BAS_DROITE.getCoordonnees(), PathfindingNodes.DEVANT_DEPART_GAUCHE.getCoordonnees()));
    }
	
    @Test
    public void test_ennemi_dans_element_jeu() throws Exception
    {
    	Assert.assertTrue(table.getObstacles()[0].isDone() == Tribool.FALSE);
    	obstaclemanager.creer_obstacle(new Vec2(1500, 150), 0);
    	Assert.assertTrue(table.getObstacles()[0].isDone() == Tribool.MAYBE);
    }

}
