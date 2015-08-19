package tests;

import java.util.ArrayList;

import org.junit.Assert;

import hook.Callback;
import hook.Hook;
import hook.HookFactory;
import hook.methods.GameElementDone;
import hook.types.HookPosition;

import org.junit.Before;
import org.junit.Test;

import permissions.ReadOnly;
import permissions.ReadWrite;
import planification.Pathfinding;
import planification.astar.AStar;
import planification.astar.arc.PathfindingNodes;
import planification.astar.arcmanager.PathfindingArcManager;
import container.ServiceNames;
import robot.RobotChrono;
import robot.RobotReal;
import strategie.GameState;
import table.GameElementNames;
import utils.ConfigInfo;
import utils.Vec2;
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
	private Pathfinding pathfinding;

	@SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        hookfactory = (HookFactory) container.getService(ServiceNames.HOOK_FACTORY);
        real_gamestate = (GameState<RobotReal,ReadWrite>) container.getService(ServiceNames.REAL_GAME_STATE);
        chrono_gamestate = GameState.cloneGameState(real_gamestate.getReadOnly());
        pathfinding = (Pathfinding)container.getService(ServiceNames.PATHFINDING);
    }
   
	@Test
	public void test_hook_vrai_avancer() throws Exception
	{
		ArrayList<Hook> hooks = new ArrayList<Hook>();
		Hook h = new HookPosition(log, null, new Vec2<ReadOnly>(456,789), 20);
		h.ajouter_callback(new Callback(new GameElementDone(null, GameElementNames.CLAP_3, Tribool.TRUE)));
		h.ajouter_callback(new Callback(new GameElementDone(null, GameElementNames.DISTRIB_1, Tribool.TRUE)));
		hooks.add(h);
		hooks.add(h);
		hooks.add(h);
		GameState.avancer(real_gamestate, 500, hooks);
/*		config.set(ConfigInfo.DATE_DEBUT_MATCH, System.currentTimeMillis());
		ArrayList<Hook> hooks_table = hookfactory.getHooksEntreScriptsReal(real_gamestate);
		GameState.setPosition(real_gamestate, new Vec2<ReadOnly>(950, 650));
		GameState.setOrientation(real_gamestate, Math.PI);
		Assert.assertEquals(GameState.isDone(real_gamestate.getReadOnly(), GameElementNames.PLOT_7), Tribool.FALSE);
		GameState.avancer(real_gamestate, 100, hooks_table);
		Assert.assertEquals(GameState.isDone(real_gamestate.getReadOnly(), GameElementNames.PLOT_7), Tribool.FALSE);
		GameState.avancer(real_gamestate, 500, hooks_table);
		Assert.assertEquals(GameState.isDone(real_gamestate.getReadOnly(), GameElementNames.PLOT_7), Tribool.TRUE);*/
	}
/*
	@Test
	public void test_hook_chrono_avancer() throws Exception
	{
		config.set(ConfigInfo.DATE_DEBUT_MATCH, System.currentTimeMillis());
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
		config.set(ConfigInfo.DATE_DEBUT_MATCH, System.currentTimeMillis());
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
*/
}
