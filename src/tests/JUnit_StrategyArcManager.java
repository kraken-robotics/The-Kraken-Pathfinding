package tests;

import hook.Hook;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import container.ServiceNames;
import astar.arc.Decision;
import astar.arc.PathfindingNodes;
import astar.arcmanager.StrategyArcManager;
import robot.RobotChrono;
import robot.RobotReal;
import scripts.Script;
import scripts.ScriptManager;
import scripts.ScriptAnticipableNames;
import strategie.GameState;

/**
 * Tests unitaires du StrategyArcManager
 * @author pf
 *
 */

public class JUnit_StrategyArcManager extends JUnit_Test {

	private StrategyArcManager strategyarcmanager;
	private GameState<RobotChrono> state;
	private ScriptManager scriptmanager;

	@Before
    public void setUp() throws Exception {
        super.setUp();
        strategyarcmanager = (StrategyArcManager) container.getService(ServiceNames.STRATEGY_ARC_MANAGER);
		@SuppressWarnings("unchecked")
		GameState<RobotReal> realstate = (GameState<RobotReal>)container.getService(ServiceNames.REAL_GAME_STATE);
		state = realstate.cloneGameState();
        scriptmanager = (ScriptManager) container.getService(ServiceNames.SCRIPT_MANAGER);
	}
	
	@Test
	public void test_iterator1() throws Exception
	{
    	state.robot.va_au_point_pathfinding(PathfindingNodes.BAS_DROITE, new ArrayList<Hook>());
		strategyarcmanager.reinitHashes();
		strategyarcmanager.reinitIterator(state);
		Assert.assertTrue(strategyarcmanager.hasNext());
		Decision d = strategyarcmanager.next();
		Assert.assertEquals(ScriptAnticipableNames.CLAP, d.script_name);
		Assert.assertEquals(0, d.version);
		Assert.assertTrue(strategyarcmanager.hasNext());
		d = strategyarcmanager.next();
		Assert.assertEquals(ScriptAnticipableNames.CLAP, d.script_name);
		Assert.assertEquals(1, d.version);
		Assert.assertTrue(strategyarcmanager.hasNext());
		d = strategyarcmanager.next();
		Assert.assertEquals(ScriptAnticipableNames.TAPIS, d.script_name);
		Assert.assertEquals(0, d.version);
		Assert.assertTrue(!strategyarcmanager.hasNext());
	}

	@Test
	public void test_iterator2() throws Exception
	{
    	Script s = scriptmanager.getScript(ScriptAnticipableNames.TAPIS);
    	state.robot.va_au_point_pathfinding(s.point_entree(0), new ArrayList<Hook>());
    	s.agit(0, state);
    	state.robot.va_au_point_pathfinding(s.point_sortie(0), new ArrayList<Hook>());
    	Script s2 = scriptmanager.getScript(ScriptAnticipableNames.ATTENTE);
    	for(int i = 0; i < 5; i++)
    	{
    		s2.agit(0, state);
			strategyarcmanager.reinitHashes();
			strategyarcmanager.reinitIterator(state);
			Assert.assertTrue(strategyarcmanager.hasNext());
			Decision d = strategyarcmanager.next();
			Assert.assertEquals(ScriptAnticipableNames.CLAP, d.script_name);
			Assert.assertEquals(0, d.version);
			Assert.assertTrue(strategyarcmanager.hasNext());
			d = strategyarcmanager.next();
			Assert.assertEquals(ScriptAnticipableNames.CLAP, d.script_name);
			Assert.assertEquals(1, d.version);
			Assert.assertTrue(!strategyarcmanager.hasNext());
    	}
	}

	@Test
	public void test_iterator3() throws Exception
	{
    	Script s = scriptmanager.getScript(ScriptAnticipableNames.CLAP);
    	state.robot.va_au_point_pathfinding(s.point_entree(0), new ArrayList<Hook>());
    	s.agit(0, state);
    	state.robot.va_au_point_pathfinding(s.point_sortie(0), new ArrayList<Hook>());
    	Script s2 = scriptmanager.getScript(ScriptAnticipableNames.ATTENTE);
    	for(int i = 0; i < 5; i++)
    	{
    		s2.agit(0, state);
			strategyarcmanager.reinitHashes();
			strategyarcmanager.reinitIterator(state);
			Assert.assertTrue(strategyarcmanager.hasNext());
			Decision d = strategyarcmanager.next();
			Assert.assertEquals(ScriptAnticipableNames.CLAP, d.script_name);
			Assert.assertEquals(1, d.version);
			Assert.assertTrue(strategyarcmanager.hasNext());
			d = strategyarcmanager.next();
			Assert.assertEquals(ScriptAnticipableNames.TAPIS, d.script_name);
			Assert.assertEquals(0, d.version);
			Assert.assertTrue(!strategyarcmanager.hasNext());
    	}
	}

	@Test
	public void test_iterator4() throws Exception
	{
    	Script s = scriptmanager.getScript(ScriptAnticipableNames.CLAP);
    	state.robot.va_au_point_pathfinding(s.point_entree(1), new ArrayList<Hook>());
    	s.agit(1, state);
    	state.robot.va_au_point_pathfinding(s.point_sortie(1), new ArrayList<Hook>());
    	Script s2 = scriptmanager.getScript(ScriptAnticipableNames.ATTENTE);
    	for(int i = 0; i < 5; i++)
    	{
    		s2.agit(0, state);
			strategyarcmanager.reinitHashes();
			strategyarcmanager.reinitIterator(state);
			Assert.assertTrue(strategyarcmanager.hasNext());
			Decision d = strategyarcmanager.next();
			Assert.assertEquals(ScriptAnticipableNames.CLAP, d.script_name);
			Assert.assertEquals(0, d.version);
			Assert.assertTrue(strategyarcmanager.hasNext());
			d = strategyarcmanager.next();
			Assert.assertEquals(ScriptAnticipableNames.TAPIS, d.script_name);
			Assert.assertEquals(0, d.version);
			Assert.assertTrue(!strategyarcmanager.hasNext());
    	}
	}

	@Test
	public void test_iterator5() throws Exception
	{
    	Script s = scriptmanager.getScript(ScriptAnticipableNames.CLAP);
    	state.robot.va_au_point_pathfinding(s.point_entree(0), new ArrayList<Hook>());
    	s.agit(0, state);
    	state.robot.va_au_point_pathfinding(s.point_entree(1), new ArrayList<Hook>());
    	s.agit(1, state);
    	state.robot.va_au_point_pathfinding(s.point_sortie(1), new ArrayList<Hook>());
    	Script s2 = scriptmanager.getScript(ScriptAnticipableNames.ATTENTE);
    	for(int i = 0; i < 5; i++)
    	{
    		s2.agit(0, state);
			strategyarcmanager.reinitHashes();
			strategyarcmanager.reinitIterator(state);
			Assert.assertTrue(strategyarcmanager.hasNext());
			Decision d = strategyarcmanager.next();
			Assert.assertEquals(ScriptAnticipableNames.TAPIS, d.script_name);
			Assert.assertEquals(0, d.version);
			Assert.assertTrue(!strategyarcmanager.hasNext());
    	}
	}

	@Test
	public void test_iterator6() throws Exception
	{
    	Script s = scriptmanager.getScript(ScriptAnticipableNames.CLAP);
    	Script s1 = scriptmanager.getScript(ScriptAnticipableNames.TAPIS);
    	state.robot.va_au_point_pathfinding(s.point_entree(0), new ArrayList<Hook>());
    	s.agit(0, state);
    	state.robot.va_au_point_pathfinding(s1.point_entree(0), new ArrayList<Hook>());
    	s1.agit(0, state);
    	state.robot.va_au_point_pathfinding(s1.point_sortie(0), new ArrayList<Hook>());
    	Script s2 = scriptmanager.getScript(ScriptAnticipableNames.ATTENTE);
    	for(int i = 0; i < 5; i++)
    	{
    		s2.agit(0, state);
			strategyarcmanager.reinitHashes();
			strategyarcmanager.reinitIterator(state);
			Assert.assertTrue(strategyarcmanager.hasNext());
			Decision d = strategyarcmanager.next();
			Assert.assertEquals(ScriptAnticipableNames.CLAP, d.script_name);
			Assert.assertEquals(1, d.version);
			Assert.assertTrue(!strategyarcmanager.hasNext());
    	}
	}

	@Test
	public void test_distanceTo() throws Exception
	{
    	Script s = scriptmanager.getScript(ScriptAnticipableNames.CLAP);
    	state.robot.va_au_point_pathfinding(s.point_entree(0), new ArrayList<Hook>());
    	ArrayList<PathfindingNodes> chemin = new ArrayList<PathfindingNodes>();
    	chemin.add(s.point_entree(0));

    	GameState<RobotChrono> state2 = state.cloneGameState();
		Decision d = new Decision(chemin, ScriptAnticipableNames.CLAP, 0);
		strategyarcmanager.distanceTo(state2, d);
		
		s.agit(0, state);
		state.robot.setPositionPathfinding(s.point_sortie(0));
		
		Assert.assertEquals(state.robot.getHash(), state2.robot.getHash());

		strategyarcmanager.reinitHashes();
		strategyarcmanager.reinitIterator(state);
		Assert.assertTrue(strategyarcmanager.hasNext());
		d = strategyarcmanager.next();
		Assert.assertEquals(ScriptAnticipableNames.CLAP, d.script_name);
		Assert.assertEquals(1, d.version);
		Assert.assertTrue(strategyarcmanager.hasNext());
		d = strategyarcmanager.next();
		Assert.assertEquals(ScriptAnticipableNames.TAPIS, d.script_name);
		Assert.assertEquals(0, d.version);
		Assert.assertTrue(!strategyarcmanager.hasNext());

		strategyarcmanager.reinitHashes();
		strategyarcmanager.reinitIterator(state2);
		Assert.assertTrue(strategyarcmanager.hasNext());
		d = strategyarcmanager.next();
		Assert.assertEquals(ScriptAnticipableNames.CLAP, d.script_name);
		Assert.assertEquals(1, d.version);
		Assert.assertTrue(strategyarcmanager.hasNext());
		d = strategyarcmanager.next();
		Assert.assertEquals(ScriptAnticipableNames.TAPIS, d.script_name);
		Assert.assertEquals(0, d.version);
		Assert.assertTrue(!strategyarcmanager.hasNext());

	}

	// TODO: test où le robot est obligé d'attendre que le robot adverse parte
	
}
