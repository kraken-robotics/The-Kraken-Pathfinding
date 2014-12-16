package tests;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import robot.RobotReal;
import scripts.Script;
import scripts.ScriptManager;
import smartMath.Vec2;
import strategie.GameState;
import enums.PathfindingNodes;
import enums.ScriptNames;
import enums.ServiceNames;

/**
 * Tests unitaires des scripts.
 * Utilisé pour voir en vrai comment agit le robot.
 * @author pf
 *
 */

// TODO: vérifier que les points d'entrée sont de basse précision

public class JUnit_Scripts extends JUnit_Test {

	private ScriptManager scriptmanager;
	private GameState<RobotReal> gamestate;
		
    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        gamestate = (GameState<RobotReal>) container.getService(ServiceNames.REAL_GAME_STATE);
        scriptmanager = (ScriptManager) container.getService(ServiceNames.SCRIPT_MANAGER);
        gamestate.robot.setPosition(new Vec2(1100, 1000));
    }
    
    @Test
    public void test_script_tapis() throws Exception
    {
    	// TODO: utiliser le pathfinding
    	Script s = scriptmanager.getScript(ScriptNames.ScriptTapis);
    	ArrayList<PathfindingNodes> chemin = new ArrayList<PathfindingNodes>(); 
    	chemin.add(s.point_entree(0));
    	gamestate.robot.suit_chemin(chemin, null);
    	s.execute(0, gamestate);
    }

    @Test
    public void test_script_clap() throws Exception
    {
    	Script s = scriptmanager.getScript(ScriptNames.ScriptClap);
    	ArrayList<PathfindingNodes> chemin = new ArrayList<PathfindingNodes>(); 
    	chemin.add(s.point_entree(0));
    	gamestate.robot.suit_chemin(chemin, null);
    	s.execute(0, gamestate);
    }

}
