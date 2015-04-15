package tests;

import hook.Hook;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;

import container.ServiceNames;
import enums.RobotColor;
import astar.AStar;
import astar.arc.PathfindingNodes;
import astar.arc.SegmentTrajectoireCourbe;
import astar.arcmanager.PathfindingArcManager;
import robot.RobotChrono;
import robot.RobotReal;
import scripts.Script;
import scripts.ScriptManager;
import scripts.ScriptAnticipableNames;
import strategie.GameState;
import utils.ConfigInfo;
import vec2.ReadOnly;
import vec2.Vec2;

/**
 * Tests unitaires des scripts.
 * Utilisé pour voir en vrai comment agit le robot et si la table est bien mise à jour.
 * @author pf
 *
 */

public class JUnit_Scripts extends JUnit_Test {

	private ScriptManager scriptmanager;
	private GameState<RobotReal> gamestate;
	private GameState<RobotChrono> state_chrono;
	private AStar<PathfindingArcManager, SegmentTrajectoireCourbe> pathfinding;

    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        gamestate = (GameState<RobotReal>) container.getService(ServiceNames.REAL_GAME_STATE);
        pathfinding = (AStar<PathfindingArcManager, SegmentTrajectoireCourbe>) container.getService(ServiceNames.A_STAR_PATHFINDING);
        scriptmanager = (ScriptManager) container.getService(ServiceNames.SCRIPT_MANAGER);
        gamestate.robot.setPosition(new Vec2<ReadOnly>(1100, 1000));
        state_chrono = gamestate.cloneGameState();
    }

    @Test
    public void test_script_tapis_chrono() throws Exception
    {
    	long hash_avant = state_chrono.getHash();
    	PathfindingNodes version = PathfindingNodes.NODE_TAPIS;
    	Script s = scriptmanager.getScript(ScriptAnticipableNames.TAPIS);
    	Assert.assertTrue(s.getVersions(state_chrono).size() == 1);
		Assert.assertEquals(state_chrono.robot.areTapisPoses()?1:0, (hash_avant >> 15) % (1 << 1));
    	state_chrono.robot.setPositionPathfinding(version);
    	s.agit(version, state_chrono);
    	Assert.assertTrue(s.getVersions(state_chrono).size() == 0);
    	Assert.assertNotEquals(hash_avant, state_chrono.getHash());
		Assert.assertEquals(state_chrono.robot.areTapisPoses()?1:0, (state_chrono.getHash() >> 15) % (1 << 1));
    }

    @Test
    public void test_script_hash() throws Exception
    {
    	GameState<RobotChrono> state_chrono2 = state_chrono.cloneGameState();
    	Script tapis = scriptmanager.getScript(ScriptAnticipableNames.TAPIS);
    	Script clap = scriptmanager.getScript(ScriptAnticipableNames.CLAP);

    	log.debug(state_chrono.robot.getPosition(), this);
    	log.debug(state_chrono2.robot.getPosition(), this);
    	Assert.assertEquals(state_chrono.getHash(), state_chrono2.getHash());

    	state_chrono.robot.setPositionPathfinding(PathfindingNodes.NODE_TAPIS);
    	state_chrono2.robot.setPositionPathfinding(PathfindingNodes.CLAP_DROIT);
    	tapis.agit(PathfindingNodes.NODE_TAPIS, state_chrono);
    	clap.agit(PathfindingNodes.CLAP_DROIT, state_chrono2);
    	log.debug(state_chrono.robot.getPosition(), this);
    	log.debug(state_chrono2.robot.getPosition(), this);
    	Assert.assertNotEquals(state_chrono.getHash(), state_chrono2.getHash());

    	state_chrono.robot.setPositionPathfinding(PathfindingNodes.CLAP_DROIT);
    	state_chrono2.robot.setPositionPathfinding(PathfindingNodes.NODE_TAPIS);
    	clap.agit(PathfindingNodes.CLAP_DROIT, state_chrono);
    	tapis.agit(PathfindingNodes.NODE_TAPIS, state_chrono2);

    	Assert.assertNotEquals(state_chrono.getHash(), state_chrono2.getHash());

    	state_chrono.robot.setPositionPathfinding(PathfindingNodes.CLAP_DROIT_SECOND);
    	state_chrono2.robot.setPositionPathfinding(PathfindingNodes.CLAP_DROIT_SECOND);
    	clap.agit(PathfindingNodes.CLAP_DROIT_SECOND, state_chrono);
    	clap.agit(PathfindingNodes.CLAP_DROIT_SECOND, state_chrono2);
    	Assert.assertEquals(state_chrono.getHash(), state_chrono2.getHash());
    }

    @Test
    public void test_script_clap_chrono() throws Exception
    {
    	long hash_avant = state_chrono.gridspace.getHashTable();
    	Script s = scriptmanager.getScript(ScriptAnticipableNames.CLAP);
    	Assert.assertTrue(s.getVersions(state_chrono).size() == 3);
    	PathfindingNodes version = PathfindingNodes.CLAP_DROIT_SECOND;
    	state_chrono.robot.setPositionPathfinding(version);
    	s.agit(version, state_chrono);
    	Assert.assertNotEquals(hash_avant, state_chrono.gridspace.getHashTable());
    	long hash_apres = state_chrono.gridspace.getHashTable();
    	Assert.assertTrue(s.getVersions(state_chrono).size() == 2);
    	version = PathfindingNodes.CLAP_DROIT;
    	state_chrono.robot.setPositionPathfinding(version);
    	s.agit(version, state_chrono);
    	Assert.assertTrue(s.getVersions(state_chrono).size() == 1);
    	Assert.assertNotEquals(hash_apres, state_chrono.gridspace.getHashTable());
    	hash_apres = state_chrono.gridspace.getHashTable();
    	version = PathfindingNodes.CLAP_GAUCHE;
    	state_chrono.robot.setPositionPathfinding(version);
    	s.agit(version, state_chrono);
    	Assert.assertTrue(s.getVersions(state_chrono).size() == 0);
    	Assert.assertNotEquals(hash_apres, state_chrono.gridspace.getHashTable());
    }

    @Test
    public void test_script_tapis() throws Exception
    {
    	gamestate.robot.setOrientation(Math.PI);
    	gamestate.robot.setPosition(PathfindingNodes.POINT_DEPART.getCoordonnees());
    	gamestate.robot.avancer(500);
    	PathfindingNodes version = PathfindingNodes.NODE_TAPIS;
    	Script s = scriptmanager.getScript(ScriptAnticipableNames.TAPIS);
    	ArrayList<SegmentTrajectoireCourbe> chemin = pathfinding.computePath(gamestate.cloneGameState(), version, true);
    	gamestate.robot.suit_chemin(chemin, new ArrayList<Hook>());
    	s.agit(version, gamestate);
    }

    @Test
    public void test_script_tapis_symetrie() throws Exception
    {
    	gamestate.robot.setOrientation(Math.PI);
    	gamestate.robot.setPosition(PathfindingNodes.POINT_DEPART.getCoordonnees());
    	gamestate.robot.avancer(500);
    	config.set(ConfigInfo.COULEUR, RobotColor.getCouleurAvecSymetrie());
    	PathfindingNodes version = PathfindingNodes.NODE_TAPIS;
    	Script s = scriptmanager.getScript(ScriptAnticipableNames.TAPIS);
    	ArrayList<SegmentTrajectoireCourbe> chemin = pathfinding.computePath(gamestate.cloneGameState(), version, true);
    	gamestate.robot.suit_chemin(chemin, new ArrayList<Hook>());
    	s.agit(version, gamestate);
    }

    @Test
    public void test_script_clap_droit() throws Exception
    {
    	gamestate.robot.setOrientation(Math.PI);
    	gamestate.robot.setPosition(PathfindingNodes.POINT_DEPART.getCoordonnees());
    	gamestate.robot.avancer(500);
    	PathfindingNodes version = PathfindingNodes.CLAP_DROIT;
    	Script s = scriptmanager.getScript(ScriptAnticipableNames.CLAP);
    	ArrayList<SegmentTrajectoireCourbe> chemin = pathfinding.computePath(gamestate.cloneGameState(), version, true);
    	gamestate.robot.suit_chemin(chemin, new ArrayList<Hook>());
    	s.agit(version, gamestate);
    }

    @Test
    public void test_script_clap_droit_second() throws Exception
    {
    	gamestate.robot.setOrientation(Math.PI);
    	gamestate.robot.setPosition(PathfindingNodes.POINT_DEPART.getCoordonnees());
    	gamestate.robot.avancer(1500);
    	PathfindingNodes version = PathfindingNodes.CLAP_DROIT_SECOND;
    	Script s = scriptmanager.getScript(ScriptAnticipableNames.CLAP);
    	ArrayList<SegmentTrajectoireCourbe> chemin = pathfinding.computePath(gamestate.cloneGameState(), version, true);
    	gamestate.robot.suit_chemin(chemin, new ArrayList<Hook>());
    	s.agit(version, gamestate);
    }

    @Test
    public void test_script_clap_gauche() throws Exception
    {
    	gamestate.robot.setOrientation(Math.PI);
    	gamestate.robot.setPosition(PathfindingNodes.POINT_DEPART.getCoordonnees());
    	gamestate.robot.avancer(500);
    	PathfindingNodes version = PathfindingNodes.CLAP_GAUCHE;
    	Script s = scriptmanager.getScript(ScriptAnticipableNames.CLAP);
    	ArrayList<SegmentTrajectoireCourbe> chemin = pathfinding.computePath(gamestate.cloneGameState(), version, true);
    	gamestate.robot.suit_chemin(chemin, new ArrayList<Hook>());
    	s.agit(version, gamestate);
    }

}
