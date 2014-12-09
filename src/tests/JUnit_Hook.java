package tests;

import java.util.ArrayList;

import org.junit.Assert;
import hook.Hook;
import hook.types.HookFactory;

import org.junit.Before;
import org.junit.Test;

import robot.RobotChrono;
import robot.RobotReal;
import smartMath.Vec2;
import strategie.GameState;
import enums.GameElementNames;
import enums.ServiceNames;
import enums.Tribool;

/**
 * Tests unitaires des hooks
 * @author pf
 *
 */

public class JUnit_Hook extends JUnit_Test {
	
	private HookFactory hookfactory;
	private GameState<RobotReal> real_gamestate;
	private GameState<RobotChrono> chrono_gamestate;

	@SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        hookfactory = (HookFactory) container.getService(ServiceNames.HOOK_FACTORY);
        real_gamestate = (GameState<RobotReal>) container.getService(ServiceNames.REAL_GAME_STATE);
        chrono_gamestate = real_gamestate.cloneGameState();
    }
   
	@Test
	public void test_hook_chrono_avancer() throws Exception
	{
		// TODO: vérifier ce test
		ArrayList<Hook> hooks_table = hookfactory.getHooksEntreScripts(chrono_gamestate);
		chrono_gamestate.robot.setPosition(new Vec2(1300, 800));
		chrono_gamestate.robot.setOrientation(Math.PI);
		Assert.assertTrue(chrono_gamestate.table.isDone(GameElementNames.VERRE_1) == Tribool.FALSE);
		chrono_gamestate.robot.avancer(500, hooks_table);
		Assert.assertTrue(chrono_gamestate.table.isDone(GameElementNames.VERRE_1) == Tribool.TRUE);
	}

}
