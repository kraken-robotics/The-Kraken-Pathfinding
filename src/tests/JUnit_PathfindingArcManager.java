package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import permissions.ReadWrite;
import container.ServiceNames;
import astar.arc.PathfindingNodes;
import astar.arc.SegmentTrajectoireCourbe;
import astar.arcmanager.PathfindingArcManager;
import robot.RobotChrono;
import robot.RobotReal;
import strategie.GameState;

/**
 * Tests unitaires des calculs sur les PathfindingNodes
 * @author pf
 *
 */

public class JUnit_PathfindingArcManager extends JUnit_Test {
	
	private PathfindingArcManager pathfindingarcmanager;
	private GameState<RobotChrono,ReadWrite> state_chrono;

	@Before
    public void setUp() throws Exception {
        super.setUp();
        pathfindingarcmanager = (PathfindingArcManager) container.getService(ServiceNames.PATHFINDING_ARC_MANAGER);
		@SuppressWarnings("unchecked")
		GameState<RobotReal,ReadWrite> state = (GameState<RobotReal,ReadWrite>)container.getService(ServiceNames.REAL_GAME_STATE);
		state_chrono = GameState.cloneGameState(state.getReadOnly());
	}

/*	@Test
	public void test_iterator() throws Exception
	{
		state_chrono.gridspace.setAvoidGameElement(false);
		state_chrono.robot.setPositionPathfinding(PathfindingNodes.BAS_DROITE);
		pathfindingarcmanager.reinitIterator(state_chrono);
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		log.debug(pathfindingarcmanager.next(), this);
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		log.debug(pathfindingarcmanager.next(), this);
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		log.debug(pathfindingarcmanager.next(), this);
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		log.debug(pathfindingarcmanager.next(), this);
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		log.debug(pathfindingarcmanager.next(), this);
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		log.debug(pathfindingarcmanager.next(), this);
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		log.debug(pathfindingarcmanager.next(), this);
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		log.debug(pathfindingarcmanager.next(), this);
		Assert.assertEquals(PathfindingNodes.SORTIE_ZONE_DEPART, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.SORTIE_CLAP_DROIT, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.SORTIE_CLAP_DROIT_SECOND, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.SORTIE_TAPIS, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.DEVANT_DEPART_DROITE, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.COTE_MARCHE_DROITE, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.DEVANT_DEPART_GAUCHE, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.BAS_GAUCHE, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.NODE_TAPIS, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.CLAP_DROIT_SECOND, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.BAS, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.SECOURS_0, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.SECOURS_1, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.SECOURS_2, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.SECOURS_3, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.SECOURS_4, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.SECOURS_5, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.SECOURS_6, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.SECOURS_7, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.SECOURS_8, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.SECOURS_9, pathfindingarcmanager.next());
		Assert.assertTrue(!pathfindingarcmanager.hasNext());
	}
	*/

	// TODO: ne passe pas le test
	
	@Test
	public void test_iterator2() throws Exception
	{
		config.setDateDebutMatch();
		boolean[] verification = new boolean[PathfindingNodes.values().length];
		for(PathfindingNodes j : PathfindingNodes.values())
		{
			GameState.setPositionPathfinding(state_chrono, j);
			pathfindingarcmanager.reinitIterator(state_chrono.getReadOnly());
			for(PathfindingNodes i : PathfindingNodes.values())
				verification[i.ordinal()] = false;
			while(pathfindingarcmanager.hasNext())
			{
				Assert.assertTrue(verification[((SegmentTrajectoireCourbe)pathfindingarcmanager.next()).objectifFinal.ordinal()] == false);
				verification[((SegmentTrajectoireCourbe)pathfindingarcmanager.next()).objectifFinal.ordinal()] = true;
			}
			for(PathfindingNodes i : PathfindingNodes.values())
				Assert.assertEquals((GameState.isTraversable(state_chrono.getReadOnly(), i, j, 0) && i != j), verification[i.ordinal()]);
		}
	}
	

	
}
