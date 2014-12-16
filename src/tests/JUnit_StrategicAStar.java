package tests;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import pathfinding.AStar;
import robot.RobotReal;
import scripts.Decision;
import scripts.ScriptManager;
import smartMath.Vec2;
import strategie.GameState;
import enums.ServiceNames;

public class JUnit_StrategicAStar extends JUnit_Test
{
	private GameState<RobotReal> gamestate;
	private AStar astar;
	
    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        gamestate = (GameState<RobotReal>) container.getService(ServiceNames.REAL_GAME_STATE);
        astar = (AStar) container.getService(ServiceNames.A_STAR);
        gamestate.robot.setPosition(new Vec2(1100, 1000));
    }
    
    @Test
    public void test_astar() throws Exception
    {
    	config.setDateDebutMatch();
//    	ArrayList<Decision> decisions = astar.computeStrategy(gamestate.cloneGameState());
//    	for(Decision d: decisions)
//    		log.debug(d.meta_version+" "+d.meta_version+" "+d.shoot_game_element, this);
    }

}
