package tests;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import container.ServiceNames;
import pathfinding.dstarlite.DStarLite;
import permissions.ReadOnly;
import tests.graphicLib.Fenetre;
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
        pathfinding = (DStarLite) container.getService(ServiceNames.D_STAR_LITE);
	}

	@Test
    public void test_chemin() throws Exception
    {
		pathfinding.computeNewPath(new Vec2<ReadOnly>(-1000, 200), new Vec2<ReadOnly>(1200, 1500));
		ArrayList<Vec2<ReadOnly>> trajet = pathfinding.itineraireBrut();
		for(Vec2<ReadOnly> v : trajet)
		{
			log.debug(v);
		}
    }

}
