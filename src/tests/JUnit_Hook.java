package tests;

import java.util.ArrayList;

import org.junit.Assert;

import hook.Hook;
import hook.HookFactory;

import org.junit.Before;
import org.junit.Test;

import container.ServiceNames;
import astar.AStar;
import astar.arc.PathfindingNodes;
import astar.arc.SegmentTrajectoireCourbe;
import astar.arcmanager.PathfindingArcManager;
import robot.RobotChrono;
import robot.RobotReal;
import strategie.GameState;
import table.GameElementNames;
import vec2.ReadOnly;
import vec2.ReadWrite;
import vec2.Vec2;
import enums.Tribool;
import exceptions.PathfindingException;

/**
 * Tests unitaires des hooks
 * @author pf
 *
 */

public class JUnit_Hook extends JUnit_Test {
	
	private HookFactory hookfactory;
	private GameState<RobotReal,ReadWrite> real_gamestate;
	private GameState<RobotChrono,ReadWrite> chrono_gamestate;
	private AStar<PathfindingArcManager, SegmentTrajectoireCourbe> pathfinding;

	@SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
		config.setDateDebutMatch();
        hookfactory = (HookFactory) container.getService(ServiceNames.HOOK_FACTORY);
        real_gamestate = (GameState<RobotReal,ReadWrite>) container.getService(ServiceNames.REAL_GAME_STATE);
        chrono_gamestate = GameState.cloneGameState(real_gamestate.getReadOnly());
        pathfinding = (AStar<PathfindingArcManager, SegmentTrajectoireCourbe>)container.getService(ServiceNames.A_STAR_PATHFINDING);
    }
   
	// TODO: ne passe pas le test
	
	@Test
	public void test_hook_vrai_avancer() throws Exception
	{
		config.setDateDebutMatch();
		ArrayList<Hook> hooks_table = hookfactory.getHooksEntreScriptsReal(real_gamestate);
		GameState.setPosition(real_gamestate, new Vec2<ReadOnly>(950, 650));
		GameState.setOrientation(real_gamestate, Math.PI);
		Assert.assertEquals(GameState.isDone(real_gamestate.getReadOnly(), GameElementNames.PLOT_7), Tribool.FALSE);
		GameState.avancer(real_gamestate, 100, hooks_table);
		Assert.assertEquals(GameState.isDone(real_gamestate.getReadOnly(), GameElementNames.PLOT_7), Tribool.FALSE);
		GameState.avancer(real_gamestate, 500, hooks_table);
		Assert.assertEquals(GameState.isDone(real_gamestate.getReadOnly(), GameElementNames.PLOT_7), Tribool.TRUE);
	}

	@Test
	public void test_hook_chrono_avancer() throws Exception
	{
		ArrayList<Hook> hooks_table = hookfactory.getHooksEntreScriptsChrono(chrono_gamestate, 90000);
		GameState.setPosition(chrono_gamestate, new Vec2<ReadOnly>(600, 350));
		GameState.setOrientation(chrono_gamestate, Math.PI);
		Assert.assertEquals(GameState.isDone(chrono_gamestate.getReadOnly(), GameElementNames.VERRE_5), Tribool.FALSE);
		GameState.avancer(chrono_gamestate, 100, hooks_table);
		Assert.assertEquals(GameState.isDone(chrono_gamestate.getReadOnly(), GameElementNames.VERRE_5), Tribool.FALSE);
		GameState.avancer(chrono_gamestate, 500, hooks_table);
		Assert.assertEquals(GameState.isDone(chrono_gamestate.getReadOnly(), GameElementNames.VERRE_5), Tribool.TRUE);
	}

	@Test
	public void test_hook_chrono_sleep() throws Exception
	{
		// TODO: vérifier par rapport à l'ancienne version
		config.setDateDebutMatch();
		GameState.setPosition(chrono_gamestate, new Vec2<ReadOnly>(1300, 500));
		ArrayList<Hook> hooks_table = hookfactory.getHooksEntreScriptsReal(real_gamestate);
		Assert.assertEquals(GameState.isDone(real_gamestate.getReadOnly(), GameElementNames.VERRE_5), Tribool.FALSE);
		GameState.sleep(chrono_gamestate, 5000, hooks_table);
		Assert.assertEquals(GameState.isDone(real_gamestate.getReadOnly(), GameElementNames.VERRE_5), Tribool.FALSE);
		GameState.sleep(chrono_gamestate, 60000, hooks_table);
		Assert.assertEquals(GameState.isDone(real_gamestate.getReadOnly(), GameElementNames.VERRE_5), Tribool.MAYBE);
	}

	@Test(expected=PathfindingException.class)
	public void test_hook_chrono_suit_chemin2() throws Exception
	{
		GameState.setPosition(chrono_gamestate, PathfindingNodes.BAS.getCoordonnees());
    	pathfinding.computePath(chrono_gamestate, PathfindingNodes.COTE_MARCHE_DROITE, false);
	}

}
