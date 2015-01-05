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
import scripts.Script;
import scripts.ScriptManager;
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
	private ScriptManager scriptmanager;
	
    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        gamestate = (GameState<RobotReal>) container.getService(ServiceNames.REAL_GAME_STATE);
        astar = (AStar<StrategyArcManager, Decision>) container.getService(ServiceNames.A_STAR_STRATEGY);
        gamestate.robot.setPosition(new Vec2(1100, 1000));
        scriptmanager = (ScriptManager) container.getService(ServiceNames.SCRIPT_MANAGER);
    }
    
    @Test
    public void test_strategy_after_decision() throws Exception
    {
    	Script s = scriptmanager.getScript(ScriptAnticipableNames.ScriptClap);
    	ArrayList<PathfindingNodes> chemin = new ArrayList<PathfindingNodes>();
    	chemin.add(s.point_entree(0));
    	Decision decision = new Decision(chemin, ScriptAnticipableNames.ScriptClap, 0);
    	GameState<RobotChrono> chronostate = gamestate.cloneGameState();
    	chronostate.robot.setPositionPathfinding(s.point_entree(0));

    	config.setDateDebutMatch();

    	ArrayList<Decision> decisions = astar.computeStrategyAfter(chronostate, decision, 10000);
		for(Decision d: decisions)
			log.debug(d, this);
    }

}
