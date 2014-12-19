package tests;

import org.junit.Before;
import org.junit.Test;

import pathfinding.AStar;
import pathfinding.StrategyArcManager;
import robot.RobotChrono;
import robot.RobotReal;
import scripts.Decision;
import smartMath.Vec2;
import strategie.GameState;
import strategie.MemoryManager;
import enums.ServiceNames;

public class JUnit_StrategicAStar extends JUnit_Test
{
	private GameState<RobotReal> gamestate;
	private AStar<StrategyArcManager, Decision> astar;
	private MemoryManager memorymanager;
	
    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        gamestate = (GameState<RobotReal>) container.getService(ServiceNames.REAL_GAME_STATE);
        astar = (AStar<StrategyArcManager, Decision>) container.getService(ServiceNames.A_STAR_STRATEGY);
        gamestate.robot.setPosition(new Vec2(1100, 1000));
        memorymanager = (MemoryManager) container.getService(ServiceNames.MEMORY_MANAGER);
    }
    
    @Test
    public void test_benchmark_strategie() throws Exception
    {
    	config.setDateDebutMatch();
		int nb_iter = 500;
		long date_avant = System.currentTimeMillis();
    	for(int k = 0; k < nb_iter; k++)
    	{
        	GameState<RobotChrono> gamestate_chrono = memorymanager.getNewGameState();
    		/*ArrayList<Decision> decisions = */astar.computeStrategy(gamestate_chrono);
    		//for(Decision d: decisions)
    		//	log.debug(d, this);
        	memorymanager.destroyGameState(gamestate_chrono);
    	}
		log.debug("Durée moyenne en µs: "+1000*(System.currentTimeMillis()-date_avant)/nb_iter, this);
    }

}
