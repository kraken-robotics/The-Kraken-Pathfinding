package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pathfinding.GridSpace;
import smartMath.Vec2;
import enums.PathfindingNodes;
import enums.ServiceNames;

/**
 * Tests unitaires de GridSpace
 * @author pf
 *
 */

public class JUnit_GridSpace extends JUnit_Test {

	private GridSpace gridspace;
	
	@Before
    public void setUp() throws Exception {
        super.setUp();
        gridspace = (GridSpace) container.getService(ServiceNames.GRID_SPACE);
    }
   
	@Test
	public void test_nearestReachableNode() throws Exception
	{
		Assert.assertEquals(PathfindingNodes.BAS_DROITE, gridspace.nearestReachableNode(PathfindingNodes.BAS_DROITE.getCoordonnees().plusNewVector(new Vec2(10, -40))));
	}

	@Test
	public void test_iterator() throws Exception
	{
		gridspace.setAvoidGameElement(false);
		gridspace.reinitIterator(PathfindingNodes.BAS_DROITE);
		Assert.assertTrue(gridspace.hasNext());
		Assert.assertEquals(PathfindingNodes.DEVANT_DEPART_DROITE, gridspace.next());
		Assert.assertTrue(gridspace.hasNext());
		Assert.assertEquals(PathfindingNodes.COTE_MARCHE_DROITE, gridspace.next());
		Assert.assertTrue(gridspace.hasNext());
		Assert.assertEquals(PathfindingNodes.DEVANT_DEPART_GAUCHE, gridspace.next());
		Assert.assertTrue(gridspace.hasNext());
		Assert.assertEquals(PathfindingNodes.NODE_TAPIS, gridspace.next());
		Assert.assertTrue(gridspace.hasNext());
		Assert.assertEquals(PathfindingNodes.CLAP_DROIT, gridspace.next());
		Assert.assertTrue(gridspace.hasNext());
		Assert.assertEquals(PathfindingNodes.BAS, gridspace.next());
		Assert.assertTrue(!gridspace.hasNext());
	}
	
	@Test
	public void test_iterator2() throws Exception
	{
		boolean[] verification = new boolean[PathfindingNodes.values().length];
		for(PathfindingNodes j : PathfindingNodes.values())
		{
			gridspace.reinitIterator(j);
			for(PathfindingNodes i : PathfindingNodes.values())
				verification[i.ordinal()] = false;
			while(gridspace.hasNext())
			{
				Assert.assertTrue(verification[gridspace.next().ordinal()] == false);
				verification[gridspace.next().ordinal()] = true;
			}
			for(PathfindingNodes i : PathfindingNodes.values())
				Assert.assertTrue((gridspace.isTraversable(i, j) && i != j) == verification[i.ordinal()]);
		}
	}
	
	@Test
	public void test_traversable() throws Exception
	{
		config.setDateDebutMatch();
		Assert.assertTrue(!gridspace.isTraversable(PathfindingNodes.BAS_DROITE, PathfindingNodes.BAS_GAUCHE));
		gridspace.setAvoidGameElement(true);
		Assert.assertTrue(!gridspace.isTraversable(PathfindingNodes.BAS_DROITE, PathfindingNodes.DEVANT_DEPART_GAUCHE));
		gridspace.setAvoidGameElement(false);
		Assert.assertTrue(gridspace.isTraversable(PathfindingNodes.BAS_DROITE, PathfindingNodes.DEVANT_DEPART_GAUCHE));

		Assert.assertTrue(gridspace.isTraversable(PathfindingNodes.NODE_TAPIS, PathfindingNodes.BAS_GAUCHE));
		gridspace.creer_obstacle(new Vec2(-220, 830));
		// mise à jour du gridspace
		gridspace.reinitConnections(0);
		Assert.assertTrue(!gridspace.isTraversable(PathfindingNodes.NODE_TAPIS, PathfindingNodes.BAS_GAUCHE));
	}

    @Test
    public void test_symetrie() throws Exception
    {
		for(PathfindingNodes i : PathfindingNodes.values())
			for(PathfindingNodes j : PathfindingNodes.values())
				Assert.assertTrue(gridspace.isTraversable(i,j) == gridspace.isTraversable(j,i));
		gridspace.reinitConnections(0);
		for(PathfindingNodes i : PathfindingNodes.values())
			for(PathfindingNodes j : PathfindingNodes.values())
				Assert.assertTrue(gridspace.isTraversable(i,j) == gridspace.isTraversable(j,i));
    }

}
