package tests;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import container.ServiceNames;
import pathfinding.DStarLite;
import permissions.ReadOnly;
import utils.Vec2;

/**
 * Tests unitaires de la recherche de chemin.
 * @author pf
 *
 */

public class JUnit_Pathfinding extends JUnit_Test {

	DStarLite pathfinding;
	
	@Before
    public void setUp() throws Exception {
        super.setUp();
        pathfinding = (DStarLite) container.getService(ServiceNames.PATHFINDING);
	}

	@Test
    public void test_chemin() throws Exception
    {
		pathfinding.computeNewPath(new Vec2<ReadOnly>(1000, 1000), new Vec2<ReadOnly>(1100, 1500));
		ArrayList<Vec2<ReadOnly>> trajet = pathfinding.itineraireBrut();
		for(Vec2<ReadOnly> v : trajet)
		{
			log.debug(v);
		}
    }

}
