package tests;

import hook.Hook;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.Before;

import container.ServiceNames;
import astar.AStar;
import astar.arc.PathfindingNodes;
import astar.arcmanager.PathfindingArcManager;
import robot.RobotReal;
import scripts.Script;
import scripts.ScriptManager;
import scripts.ScriptAnticipableNames;
import strategie.GameState;
import utils.ConfigInfo;
import utils.Vec2;

/**
 * Tests unitaires des scripts.
 * Utilisé pour voir en vrai comment agit le robot et si la table est bien mise à jour.
 * @author pf
 *
 */

public class JUnit_Script_Symetrie extends JUnit_Test {

	private ScriptManager scriptmanager;
	private GameState<RobotReal> gamestate;
	private AStar<PathfindingArcManager, PathfindingNodes> pathfinding;

    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        config.set(ConfigInfo.COULEUR, "jaune");
        gamestate = (GameState<RobotReal>) container.getService(ServiceNames.REAL_GAME_STATE);
        pathfinding = (AStar<PathfindingArcManager, PathfindingNodes>) container.getService(ServiceNames.A_STAR_PATHFINDING);
        scriptmanager = (ScriptManager) container.getService(ServiceNames.SCRIPT_MANAGER);
        gamestate.robot.setPosition(new Vec2(1100, 1000));
    }

    @Test
    public void test_script_tapis() throws Exception
    {
    	gamestate.robot.setOrientation(Math.PI);
    	gamestate.robot.setPosition(PathfindingNodes.POINT_DEPART.getCoordonnees());
    	gamestate.robot.avancer(500);
    	int version = 0;
    	Script s = scriptmanager.getScript(ScriptAnticipableNames.TAPIS);
    	ArrayList<PathfindingNodes> chemin = pathfinding.computePath(gamestate.cloneGameState(), s.point_entree(version), true);
    	gamestate.robot.suit_chemin(chemin, new ArrayList<Hook>());
    	s.agit(version, gamestate);
    }

    @Test
    public void test_script_tapis_symetrie() throws Exception
    {
    	gamestate.robot.setOrientation(Math.PI);
    	gamestate.robot.setPosition(PathfindingNodes.POINT_DEPART.getCoordonnees());
    	gamestate.robot.avancer(500);
    	config.set(ConfigInfo.COULEUR, "jaune");
    	int version = 0;
    	Script s = scriptmanager.getScript(ScriptAnticipableNames.TAPIS);
    	ArrayList<PathfindingNodes> chemin = pathfinding.computePath(gamestate.cloneGameState(), s.point_entree(version), true);
    	gamestate.robot.suit_chemin(chemin, new ArrayList<Hook>());
    	s.agit(version, gamestate);
    }

    @Test
    public void test_script_clap_droit() throws Exception
    {
    	gamestate.robot.setOrientation(Math.PI);
    	gamestate.robot.setPosition(PathfindingNodes.POINT_DEPART.getCoordonnees());
    	gamestate.robot.avancer(500);
    	int version = 0;
    	Script s = scriptmanager.getScript(ScriptAnticipableNames.CLAP);
    	ArrayList<PathfindingNodes> chemin = pathfinding.computePath(gamestate.cloneGameState(), s.point_entree(version), true);
    	gamestate.robot.suit_chemin(chemin, new ArrayList<Hook>());
    	s.agit(version, gamestate);
    }

    @Test
    public void test_script_clap_droit_second() throws Exception
    {
    	gamestate.robot.setOrientation(Math.PI);
    	gamestate.robot.setPosition(PathfindingNodes.POINT_DEPART.getCoordonnees());
    	gamestate.robot.avancer(500);
    	int version = 1;
    	Script s = scriptmanager.getScript(ScriptAnticipableNames.CLAP);
    	ArrayList<PathfindingNodes> chemin = pathfinding.computePath(gamestate.cloneGameState(), s.point_entree(version), true);
    	gamestate.robot.suit_chemin(chemin, new ArrayList<Hook>());
    	s.agit(version, gamestate);
    }

    @Test
    public void test_script_clap_gauche() throws Exception
    {
    	gamestate.robot.setOrientation(Math.PI);
    	gamestate.robot.setPosition(PathfindingNodes.POINT_DEPART.getCoordonnees());
    	gamestate.robot.avancer(500);
    	int version = 2;
    	Script s = scriptmanager.getScript(ScriptAnticipableNames.CLAP);
    	ArrayList<PathfindingNodes> chemin = pathfinding.computePath(gamestate.cloneGameState(), s.point_entree(version), true);
    	gamestate.robot.suit_chemin(chemin, new ArrayList<Hook>());
    	s.agit(version, gamestate);
    }

}
