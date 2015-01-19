package tests;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import container.ServiceNames;
import astar.AStar;
import astar.arc.Decision;
import astar.arc.PathfindingNodes;
import astar.arcmanager.StrategyArcManager;
import robot.RobotChrono;
import robot.RobotReal;
import scripts.ScriptAnticipableNames;
import strategie.GameState;
import utils.Vec2;

/**
 * Tests unitaires du planificateur de scripts
 * @author pf
 *
 */

public class JUnit_StrategicAStar extends JUnit_Test
{
	private GameState<RobotReal> gamestate;
	private AStar<StrategyArcManager, Decision> astar;
	
    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        gamestate = (GameState<RobotReal>) container.getService(ServiceNames.REAL_GAME_STATE);
        astar = (AStar<StrategyArcManager, Decision>) container.getService(ServiceNames.A_STAR_STRATEGY);
        gamestate.robot.setPosition(new Vec2(1100, 1000));
    }
    
    @Test
    public void test_strategy_after_decision() throws Exception
    {
    	PathfindingNodes version = PathfindingNodes.CLAP_DROIT;
    	ArrayList<PathfindingNodes> chemin = new ArrayList<PathfindingNodes>();
    	chemin.add(version);
    	Decision decision = new Decision(chemin, ScriptAnticipableNames.CLAP, version);
    	GameState<RobotChrono> chronostate = gamestate.cloneGameState();
    	chronostate.robot.setPositionPathfinding(version);

    	config.setDateDebutMatch();

    	ArrayList<Decision> decisions = astar.computeStrategyAfter(chronostate, decision, 10000);
		for(Decision d: decisions)
			log.debug(d, this);
    }

}
