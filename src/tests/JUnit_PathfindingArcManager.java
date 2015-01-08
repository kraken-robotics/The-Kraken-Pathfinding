package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import container.ServiceNames;
import astar.arc.PathfindingNodes;
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
	private GameState<RobotChrono> state_chrono;

	@Before
    public void setUp() throws Exception {
        super.setUp();
        pathfindingarcmanager = (PathfindingArcManager) container.getService(ServiceNames.PATHFINDING_ARC_MANAGER);
		@SuppressWarnings("unchecked")
		GameState<RobotReal> state = (GameState<RobotReal>)container.getService(ServiceNames.REAL_GAME_STATE);
		state_chrono = state.cloneGameState();
	}

	@Test
	public void test_iterator() throws Exception
	{
		state_chrono.gridspace.setAvoidGameElement(false);
		state_chrono.robot.setPositionPathfinding(PathfindingNodes.BAS_DROITE);
		pathfindingarcmanager.reinitIterator(state_chrono);
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.DEVANT_DEPART_DROITE, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.COTE_MARCHE_DROITE, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.DEVANT_DEPART_GAUCHE, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.NODE_TAPIS, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.CLAP_DROIT, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.BAS, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.SORTIE_ZONE_DEPART, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.SORTIE_CLAP_DROIT, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.SORTIE_TAPIS, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.SECOURS_0, pathfindingarcmanager.next());
		Assert.assertTrue(pathfindingarcmanager.hasNext());
		Assert.assertEquals(PathfindingNodes.SECOURS_1, pathfindingarcmanager.next());
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
	
	@Test
	public void test_iterator2() throws Exception
	{
		config.setDateDebutMatch();
		boolean[] verification = new boolean[PathfindingNodes.values().length];
		for(PathfindingNodes j : PathfindingNodes.values())
		{
			state_chrono.robot.setPositionPathfinding(j);
			pathfindingarcmanager.reinitIterator(state_chrono);
			for(PathfindingNodes i : PathfindingNodes.values())
				verification[i.ordinal()] = false;
			while(pathfindingarcmanager.hasNext())
			{
				Assert.assertTrue(verification[pathfindingarcmanager.next().ordinal()] == false);
				verification[pathfindingarcmanager.next().ordinal()] = true;
			}
			for(PathfindingNodes i : PathfindingNodes.values())
				Assert.assertEquals((state_chrono.gridspace.isTraversable(i, j, 0) && i != j), verification[i.ordinal()]);
		}
	}
	

	
}
