package tests;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;

import container.ServiceNames;
import astar.AStar;
import astar.arc.PathfindingNodes;
import astar.arcmanager.PathfindingArcManager;
import robot.RobotChrono;
import robot.RobotReal;
import scripts.Script;
import scripts.ScriptManager;
import scripts.ScriptNames;
import strategie.GameState;
import utils.Vec2;

/**
 * Tests unitaires des scripts.
 * Utilisé pour voir en vrai comment agit le robot et si la table est bien mise à jour.
 * @author pf
 *
 */

// TODO: vérifier que les points d'entrée sont de basse précision

public class JUnit_Scripts extends JUnit_Test {

	private ScriptManager scriptmanager;
	private GameState<RobotReal> gamestate;
	private GameState<RobotChrono> state_chrono;
	private AStar<PathfindingArcManager, PathfindingNodes> pathfinding;

    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        gamestate = (GameState<RobotReal>) container.getService(ServiceNames.REAL_GAME_STATE);
        pathfinding = (AStar<PathfindingArcManager, PathfindingNodes>) container.getService(ServiceNames.A_STAR_PATHFINDING);
        scriptmanager = (ScriptManager) container.getService(ServiceNames.SCRIPT_MANAGER);
        gamestate.robot.setPosition(new Vec2(1100, 1000));
        state_chrono = gamestate.cloneGameState();
    }

    @Test
    public void test_script_tapis_chrono() throws Exception
    {
    	long hash_avant = state_chrono.getHash();
    	Script s = scriptmanager.getScript(ScriptNames.ScriptTapis);
    	Assert.assertTrue(s.getVersions(state_chrono).size() == 1);
		Assert.assertEquals(state_chrono.robot.areTapisPoses()?1:0, (hash_avant >> 15) % (1 << 1));
    	state_chrono.robot.setPositionPathfinding(s.point_entree(0));
    	s.agit(0, state_chrono);
    	Assert.assertTrue(s.getVersions(state_chrono).size() == 0);
    	Assert.assertNotEquals(hash_avant, state_chrono.getHash());
		Assert.assertEquals(state_chrono.robot.areTapisPoses()?1:0, (state_chrono.getHash() >> 15) % (1 << 1));
    }

    @Test
    public void test_script_hash() throws Exception
    {
    	GameState<RobotChrono> state_chrono2 = state_chrono.cloneGameState();
    	Script tapis = scriptmanager.getScript(ScriptNames.ScriptTapis);
    	Script clap = scriptmanager.getScript(ScriptNames.ScriptClap);

    	log.debug(state_chrono.robot.getPosition(), this);
    	log.debug(state_chrono2.robot.getPosition(), this);
    	Assert.assertEquals(state_chrono.getHash(), state_chrono2.getHash());

    	state_chrono.robot.setPositionPathfinding(tapis.point_entree(0));
    	state_chrono2.robot.setPositionPathfinding(clap.point_entree(1));
    	tapis.agit(0, state_chrono);
    	clap.agit(1, state_chrono2);
    	log.debug(state_chrono.robot.getPosition(), this);
    	log.debug(state_chrono2.robot.getPosition(), this);
    	Assert.assertNotEquals(state_chrono.getHash(), state_chrono2.getHash());

    	state_chrono.robot.setPositionPathfinding(clap.point_entree(1));
    	state_chrono2.robot.setPositionPathfinding(tapis.point_entree(0));
    	clap.agit(1, state_chrono);
    	tapis.agit(0, state_chrono2);

    	Assert.assertNotEquals(state_chrono.getHash(), state_chrono2.getHash());

    	state_chrono.robot.setPositionPathfinding(clap.point_entree(0));
    	state_chrono2.robot.setPositionPathfinding(clap.point_entree(0));
    	clap.agit(0, state_chrono);
    	clap.agit(0, state_chrono2);
    	Assert.assertEquals(state_chrono.getHash(), state_chrono2.getHash());
    }

    @Test
    public void test_script_clap_chrono() throws Exception
    {
    	long hash_avant = state_chrono.gridspace.getHashTable();
    	Script s = scriptmanager.getScript(ScriptNames.ScriptClap);
    	Assert.assertTrue(s.getVersions(state_chrono).size() == 2);
    	state_chrono.robot.setPositionPathfinding(s.point_entree(0));
    	s.agit(0, state_chrono);
    	Assert.assertNotEquals(hash_avant, state_chrono.gridspace.getHashTable());
    	long hash_apres = state_chrono.gridspace.getHashTable();
    	Assert.assertTrue(s.getVersions(state_chrono).size() == 1);
    	state_chrono.robot.setPositionPathfinding(s.point_entree(1));
    	s.agit(1, state_chrono);
    	Assert.assertTrue(s.getVersions(state_chrono).size() == 0);
    	Assert.assertNotEquals(hash_apres, state_chrono.gridspace.getHashTable());
    }

    @Test
    public void test_script_tapis() throws Exception
    {
    	int version = 0;
    	Script s = scriptmanager.getScript(ScriptNames.ScriptTapis);
    	ArrayList<PathfindingNodes> chemin = pathfinding.computePath(gamestate.cloneGameState(), s.point_entree(version), true);
    	gamestate.robot.suit_chemin(chemin, null);
    	s.agit(version, gamestate);
    }

    @Test
    public void test_script_clap() throws Exception
    {
    	int version = 0;
    	Script s = scriptmanager.getScript(ScriptNames.ScriptClap);
    	ArrayList<PathfindingNodes> chemin = pathfinding.computePath(gamestate.cloneGameState(), s.point_entree(version), true);
    	gamestate.robot.suit_chemin(chemin, null);
    	s.agit(1, gamestate);
    }

}
