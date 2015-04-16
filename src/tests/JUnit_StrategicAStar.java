package tests;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import permissions.ReadOnly;
import permissions.ReadWrite;
import container.ServiceNames;
import astar.AStar;
import astar.arc.Decision;
import astar.arc.PathfindingNodes;
import astar.arc.SegmentTrajectoireCourbe;
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
	private GameState<RobotReal,ReadWrite> gamestate;
	private AStar<StrategyArcManager, Decision> astar;
	
    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        gamestate = (GameState<RobotReal,ReadWrite>) container.getService(ServiceNames.REAL_GAME_STATE);
        astar = (AStar<StrategyArcManager, Decision>) container.getService(ServiceNames.A_STAR_STRATEGY);
        GameState.setPosition(gamestate,new Vec2<ReadOnly>(1100, 1000));
    }
    
    @Test
    public void test_strategy_after_decision() throws Exception
    {
    	PathfindingNodes version = PathfindingNodes.CLAP_DROIT;
    	ArrayList<SegmentTrajectoireCourbe> chemin = new ArrayList<SegmentTrajectoireCourbe>();
    	chemin.add(new SegmentTrajectoireCourbe(version));
    	Decision decision = new Decision(chemin, ScriptAnticipableNames.SORTIE_ZONE_DEPART, version);
    	GameState<RobotChrono,ReadWrite> chronostate = GameState.cloneGameState(gamestate.getReadOnly());
    	GameState.setPositionPathfinding(chronostate, version);

    	config.setDateDebutMatch();

    	ArrayList<Decision> decisions = astar.computeStrategyAfter(chronostate.getReadOnly(), decision, 10000);
		for(Decision d: decisions)
			log.debug(d);
    }

}
