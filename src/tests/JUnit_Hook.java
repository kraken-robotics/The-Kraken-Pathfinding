package tests;

import hook.HookFactory;

import org.junit.Before;

import pathfinding.GameState;
import permissions.ReadWrite;
import container.ServiceNames;
import robot.RobotChrono;
import robot.RobotReal;

/**
 * Tests unitaires des hooks
 * @author pf
 *
 */

public class JUnit_Hook extends JUnit_Test {
	
	private HookFactory hookfactory;
	private GameState<RobotReal,ReadWrite> real_gamestate;
	private GameState<RobotChrono,ReadWrite> chrono_gamestate;

	@SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        hookfactory = (HookFactory) container.getService(ServiceNames.HOOK_FACTORY);
        real_gamestate = (GameState<RobotReal,ReadWrite>) container.getService(ServiceNames.REAL_GAME_STATE);
        chrono_gamestate = GameState.cloneGameState(real_gamestate.getReadOnly());
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
