package tests;

import hook.Hook;
import hook.types.HookFactory;

import java.util.ArrayList;
import java.util.Random;

import obstacles.ObstacleManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import pathfinding.AStar;
import enums.GameElementNames;
import enums.PathfindingNodes;
import enums.ServiceNames;
import enums.Tribool;
import robot.RobotChrono;
import robot.RobotReal;
import smartMath.Vec2;
import strategie.GameState;
import tests.graphicLib.Fenetre;
import utils.Sleep;

public class JUnit_Test_Graphic extends JUnit_Test {

	private Fenetre fenetre;
	private ObstacleManager obstaclemanager;
	private AStar pathfinding;
	private GameState<RobotChrono> state_chrono;
	private GameState<RobotReal> state;
	private HookFactory hookfactory;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
        pathfinding = (AStar) container.getService(ServiceNames.A_STAR);
		obstaclemanager = (ObstacleManager) container.getService(ServiceNames.OBSTACLE_MANAGER);
		state = (GameState<RobotReal>)container.getService(ServiceNames.REAL_GAME_STATE);
        hookfactory = (HookFactory) container.getService(ServiceNames.HOOK_FACTORY);

		fenetre = new Fenetre();
		fenetre.setDilatationObstacle(obstaclemanager.getDilatationObstacle());
		for(PathfindingNodes n : PathfindingNodes.values())
		{
			fenetre.addPoint(n.getCoordonnees());
			for(PathfindingNodes m : PathfindingNodes.values())
				if(!obstaclemanager.obstacle_fixe_dans_segment_pathfinding(n.getCoordonnees(), m.getCoordonnees()))
						fenetre.addSegment(m.getCoordonnees(), n.getCoordonnees());
		}
		updateAffichage();
		fenetre.showOnFrame();
	}
	
	public void updateAffichage()
	{
		fenetre.setGameElement(obstaclemanager.getListGameElement());
		fenetre.setObstaclesFixes(obstaclemanager.getListObstaclesFixes());
		fenetre.setObstaclesMobiles(obstaclemanager.getListObstaclesMobiles());		
	}

	@Test
    public void test_verification_humaine() throws Exception
    {
		Random randomgenerator = new Random();
		for(int k = 0; k < 20; k++)
		{
			PathfindingNodes i = PathfindingNodes.values()[randomgenerator.nextInt(PathfindingNodes.values().length)];
			PathfindingNodes j = PathfindingNodes.values()[randomgenerator.nextInt(PathfindingNodes.values().length)];
			log.debug("Recherche chemin entre "+i+" et "+j, this);
			Vec2 entree = i.getCoordonnees().plusNewVector(new Vec2(randomgenerator.nextInt(100)-50, randomgenerator.nextInt(100)-50));
			config.setDateDebutMatch(); // afin d'avoir toujours une haute précision
			state_chrono = state.cloneGameState();
			double orientation_initiale = state_chrono.robot.getOrientation();
			state_chrono.robot.setPosition(entree);
			ArrayList<PathfindingNodes> chemin = pathfinding.computePath(state_chrono, j, true, true);
    		ArrayList<Vec2> cheminVec2 = new ArrayList<Vec2>();
    		cheminVec2.add(entree);
    		for(PathfindingNodes n: chemin)
    		{
    			log.debug(n, this);
    			cheminVec2.add(n.getCoordonnees());
    		}
    		fenetre.setPath(orientation_initiale, cheminVec2);
    		fenetre.repaint();
    		Sleep.sleep(1000);
		}
    }

	@Test
	public void test_hook_chrono_suit_chemin() throws Exception
	{
		state_chrono = state.cloneGameState();
		ArrayList<Hook> hooks_table = hookfactory.getHooksEntreScripts(state_chrono);
		state_chrono.robot.setPosition(PathfindingNodes.BAS.getCoordonnees().plusNewVector(new Vec2(10, 10)));
		double orientation_initiale = state_chrono.robot.getOrientation();
    	ArrayList<PathfindingNodes> chemin = pathfinding.computePath(state_chrono, PathfindingNodes.COTE_MARCHE_DROITE, true, false);

		ArrayList<Vec2> cheminVec2 = new ArrayList<Vec2>();
		cheminVec2.add(PathfindingNodes.BAS.getCoordonnees().plusNewVector(new Vec2(10, 10)));
		for(PathfindingNodes n: chemin)
		{
			log.debug(n, this);
			cheminVec2.add(n.getCoordonnees());
		}
		fenetre.setPath(orientation_initiale, cheminVec2);
		fenetre.repaint();
    	
		Assert.assertEquals(PathfindingNodes.BAS.getCoordonnees().plusNewVector(new Vec2(10, 10)), state_chrono.robot.getPosition());
		Assert.assertTrue(state_chrono.table.isDone(GameElementNames.PLOT_6) == Tribool.FALSE);
		state_chrono.robot.suit_chemin(chemin, hooks_table);
		Assert.assertTrue(state_chrono.table.isDone(GameElementNames.PLOT_6) == Tribool.TRUE);
		Assert.assertEquals(PathfindingNodes.COTE_MARCHE_DROITE.getCoordonnees(), state_chrono.robot.getPosition());
		
    	// on vérifie qu'à présent qu'on a emprunté ce chemin, il n'y a plus d'élément de jeu dessus et donc qu'on peut demander un pathfinding sans exception
    	state_chrono.robot.setPosition(PathfindingNodes.BAS.getCoordonnees());
    	pathfinding.computePath(state_chrono, PathfindingNodes.COTE_MARCHE_DROITE, false, false);
	}

}
