package tests;

import java.util.ArrayList;

import org.junit.Assert;

import hook.Hook;
import hook.types.HookFactory;

import org.junit.Before;
import org.junit.Test;

import pathfinding.AStar;
import robot.RobotChrono;
import robot.RobotReal;
import smartMath.Vec2;
import strategie.GameState;
import enums.GameElementNames;
import enums.PathfindingNodes;
import enums.ServiceNames;
import enums.Tribool;
import exceptions.PathfindingException;

/**
 * Tests unitaires des hooks
 * @author pf
 *
 */

public class JUnit_Hook extends JUnit_Test {
	
	private HookFactory hookfactory;
	private GameState<RobotReal> real_gamestate;
	private GameState<RobotChrono> chrono_gamestate;
	private AStar pathfinding;

	@SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
		config.setDateDebutMatch();
        hookfactory = (HookFactory) container.getService(ServiceNames.HOOK_FACTORY);
        real_gamestate = (GameState<RobotReal>) container.getService(ServiceNames.REAL_GAME_STATE);
        chrono_gamestate = real_gamestate.cloneGameState();
        pathfinding = (AStar)container.getService(ServiceNames.A_STAR);
    }
   
	@Test
	public void test_hook_vrai_avancer() throws Exception
	{
		config.setDateDebutMatch();
		ArrayList<Hook> hooks_table = hookfactory.getHooksEntreScripts(real_gamestate);
		real_gamestate.robot.setPosition(new Vec2(600, 350));
		real_gamestate.robot.setOrientation(Math.PI);
		Assert.assertTrue(real_gamestate.table.isDone(GameElementNames.VERRE_5) == Tribool.FALSE);
		real_gamestate.robot.avancer(100, hooks_table);
		Assert.assertTrue(real_gamestate.table.isDone(GameElementNames.VERRE_5) == Tribool.FALSE);
		real_gamestate.robot.avancer(500, hooks_table);
		Assert.assertTrue(real_gamestate.table.isDone(GameElementNames.VERRE_5) == Tribool.TRUE);
	}

	@Test
	public void test_hook_chrono_avancer() throws Exception
	{
		ArrayList<Hook> hooks_table = hookfactory.getHooksEntreScripts(chrono_gamestate);
		chrono_gamestate.robot.setPosition(new Vec2(600, 350));
		chrono_gamestate.robot.setOrientation(Math.PI);
		Assert.assertTrue(chrono_gamestate.table.isDone(GameElementNames.VERRE_5) == Tribool.FALSE);
		chrono_gamestate.robot.avancer(100, hooks_table);
		Assert.assertTrue(chrono_gamestate.table.isDone(GameElementNames.VERRE_5) == Tribool.FALSE);
		chrono_gamestate.robot.avancer(500, hooks_table);
		Assert.assertTrue(chrono_gamestate.table.isDone(GameElementNames.VERRE_5) == Tribool.TRUE);
	}

	@Test
	public void test_hook_chrono_sleep() throws Exception
	{
		config.setDateDebutMatch();
		ArrayList<Hook> hooks_table = hookfactory.getHooksEntreScripts(real_gamestate);
		Assert.assertTrue(real_gamestate.table.isDone(GameElementNames.VERRE_5) == Tribool.FALSE);
		chrono_gamestate.robot.sleep(5000, hooks_table);
		Assert.assertTrue(real_gamestate.table.isDone(GameElementNames.VERRE_5) == Tribool.FALSE);
		chrono_gamestate.robot.sleep(90000, hooks_table);
		Assert.assertTrue(real_gamestate.table.isDone(GameElementNames.VERRE_5) == Tribool.MAYBE);
	}

	@Test(expected=PathfindingException.class)
	public void test_hook_chrono_suit_chemin2() throws Exception
	{
		chrono_gamestate.robot.setPosition(PathfindingNodes.BAS.getCoordonnees());
    	pathfinding.computePath(chrono_gamestate, PathfindingNodes.COTE_MARCHE_DROITE, false);
	}

}
