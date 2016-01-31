package tests;

import org.junit.Before;
import org.junit.Test;

import container.ServiceNames;
import pathfinding.GameState;
import permissions.ReadWrite;
import robot.RobotReal;
import scripts.ScriptAnticipableNames;
import scripts.ScriptManager;
import utils.Sleep;

/**
 * Test de script
 * @author pf
 *
 */

public class JUnit_Script extends JUnit_Test
{
	private GameState<RobotReal,ReadWrite> realstate;
	private ScriptManager scripts;
	
	@SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        realstate = (GameState<RobotReal,ReadWrite>)container.getService(ServiceNames.REAL_GAME_STATE);
        scripts = (ScriptManager) container.getService(ServiceNames.SCRIPT_MANAGER);
    }

    @Test
    public void test_script() throws Exception
    {
    	scripts.getScript(ScriptAnticipableNames.SORTIE_ZONE_DEPART).agit(0, realstate);
    	Sleep.sleep(1000);
    }
}