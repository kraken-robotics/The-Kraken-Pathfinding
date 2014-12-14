package tests;

import org.junit.Before;
import org.junit.Test;

import pathfinding.Pathfinding;
import robot.RobotChrono;
import robot.RobotReal;
import smartMath.Vec2;
import strategie.GameState;
import enums.GameElementNames;
import enums.PathfindingNodes;
import enums.ServiceNames;
import exceptions.PathfindingException;
import exceptions.PathfindingRobotInObstacleException;

/**
 * Tests unitaires de la recherche de chemin.
 * @author pf
 *
 */

public class JUnit_Pathfinding extends JUnit_Test {

	private Pathfinding pathfinding;
	private GameState<RobotChrono> state_chrono;
	
	@Before
    public void setUp() throws Exception {
        super.setUp();
        pathfinding = (Pathfinding) container.getService(ServiceNames.PATHFINDING);
		@SuppressWarnings("unchecked")
		GameState<RobotReal> state = (GameState<RobotReal>)container.getService(ServiceNames.REAL_GAME_STATE);
		state_chrono = state.cloneGameState();
	}

	@Test(expected=PathfindingRobotInObstacleException.class)
    public void test_robot_dans_obstacle() throws Exception
    {
		state_chrono.robot.setPosition(new Vec2(80, 80));
    	state_chrono.gridspace.creer_obstacle(new Vec2(80, 80));
    	pathfinding.computePath(state_chrono, PathfindingNodes.values()[0], false, false);
    }

	@Test(expected=PathfindingException.class)
    public void test_obstacle() throws Exception
    {
		state_chrono.robot.setPosition(new Vec2(80, 80));
		state_chrono.gridspace.creer_obstacle(PathfindingNodes.values()[0].getCoordonnees());
    	pathfinding.computePath(state_chrono, PathfindingNodes.values()[0], false, false);
    }

	@Test
    public void test_brute_force() throws Exception
    {
    	for(PathfindingNodes i: PathfindingNodes.values())
        	for(PathfindingNodes j: PathfindingNodes.values())
        		if(!j.is_an_emergency_point()) // on n'arrive jamais dans un emergency point
        		{
        			state_chrono.robot.setPosition(i.getCoordonnees());
        			pathfinding.computePath(state_chrono, j, false, true);
        			pathfinding.computePath(state_chrono, j, true, true);
        		}
    }
	
	@Test
    public void test_element_jeu_disparu() throws Exception
    {
    	// une fois ce verre pris, le chemin est libre
    	state_chrono.table.setDone(GameElementNames.VERRE_3);
    	state_chrono.robot.setPositionPathfinding(PathfindingNodes.BAS_GAUCHE);
    	pathfinding.computePath(state_chrono, PathfindingNodes.COTE_MARCHE_GAUCHE, false, false);
    }

	@Test(expected=PathfindingException.class)
    public void test_element_jeu_disparu_2() throws Exception
    {
		// Exception car il y a un verre sur le passage
		state_chrono.robot.setPositionPathfinding(PathfindingNodes.BAS_GAUCHE);
    	pathfinding.computePath(state_chrono, PathfindingNodes.COTE_MARCHE_GAUCHE, false, false);
    }
	
	@Test
    public void test_element_jeu_disparu_3() throws Exception
    {
		// Pas d'exception car on demande au pathfinding de passer sur les éléments de jeux.
		state_chrono.robot.setPositionPathfinding(PathfindingNodes.BAS_GAUCHE);
    	pathfinding.computePath(state_chrono, PathfindingNodes.COTE_MARCHE_GAUCHE, false, true);
    }

}
