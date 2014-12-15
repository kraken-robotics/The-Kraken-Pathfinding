package tests;

import java.util.ArrayList;
import java.util.Random;

import obstacles.ObstacleManager;

import org.junit.Before;
import org.junit.Test;

import pathfinding.AStar;
import enums.PathfindingNodes;
import enums.ServiceNames;
import robot.RobotChrono;
import robot.RobotReal;
import smartMath.Vec2;
import strategie.GameState;
import tests.graphicLib.Fenetre;
import utils.Sleep;

public class JUnit_Test_Graphic extends JUnit_Test {

	Fenetre fenetre;
	ObstacleManager obstaclemanager;
	private AStar pathfinding;
	private GameState<RobotChrono> state_chrono;
	
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
        pathfinding = (AStar) container.getService(ServiceNames.A_STAR);
		obstaclemanager = (ObstacleManager) container.getService(ServiceNames.OBSTACLE_MANAGER);
		@SuppressWarnings("unchecked")
		GameState<RobotReal> state = (GameState<RobotReal>)container.getService(ServiceNames.REAL_GAME_STATE);
		state_chrono = state.cloneGameState();

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
			state_chrono.robot.setPosition(entree);
			ArrayList<PathfindingNodes> chemin = pathfinding.computePath(state_chrono, j, true);
    		ArrayList<Vec2> cheminVec2 = new ArrayList<Vec2>();
    		cheminVec2.add(entree);
    		for(PathfindingNodes n: chemin)
    		{
    			log.debug(n, this);
    			cheminVec2.add(n.getCoordonnees());
    		}
    		fenetre.setPath(cheminVec2);
    		fenetre.repaint();
    		Sleep.sleep(2000);
		}
    }


}
