package tests;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import pathfinding.AStar;
import pathfinding.StrategyArcManager;
import robot.RobotReal;
import scripts.Decision;
import smartMath.Vec2;
import strategie.GameState;
import enums.ServiceNames;

public class JUnit_StrategicAStar extends JUnit_Test
{
	private GameState<RobotReal> gamestate;
	private AStar<StrategyArcManager> astar;
	
    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        gamestate = (GameState<RobotReal>) container.getService(ServiceNames.REAL_GAME_STATE);
        astar = (AStar<StrategyArcManager>) container.getService(ServiceNames.A_STAR_STRATEGY);
        gamestate.robot.setPosition(new Vec2(1100, 1000));
    }
    
    @Test
    public void test_astar() throws Exception
    {
    	config.setDateDebutMatch();
    	ArrayList<Decision> decisions = astar.computeStrategy(gamestate.cloneGameState());
    	for(Decision d: decisions)
    		log.debug(d, this);
    }

}
