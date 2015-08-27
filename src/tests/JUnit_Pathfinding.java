package tests;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import container.ServiceNames;
import pathfinding.dstarlite.DStarLite;
import pathfinding.thetastar.CheminPathfinding;
import pathfinding.thetastar.LocomotionArc;
import pathfinding.thetastar.ThetaStar;
import permissions.ReadOnly;
import utils.Vec2;

/**
 * Tests unitaires de la recherche de chemin.
 * @author pf
 *
 */

public class JUnit_Pathfinding extends JUnit_Test {

	DStarLite pathfinding;
	ThetaStar pathfindingCourbe;
	CheminPathfinding chemin;
	
	@Before
    public void setUp() throws Exception {
        super.setUp();
        pathfinding = (DStarLite) container.getService(ServiceNames.D_STAR_LITE);
        pathfindingCourbe = (ThetaStar) container.getService(ServiceNames.THETA_STAR);
        chemin = (CheminPathfinding) container.getService(ServiceNames.CHEMIN_PATHFINDING);
	}

	@Test
    public void test_chemin_dstarlite() throws Exception
    {
		pathfinding.computeNewPath(new Vec2<ReadOnly>(-1000, 200), new Vec2<ReadOnly>(1200, 1500));
		ArrayList<Vec2<ReadOnly>> trajet = pathfinding.itineraireBrut();
		for(Vec2<ReadOnly> v : trajet)
		{
			log.debug(v);
		}
    }

	@Test
    public void test_chemin_thetastar() throws Exception
    {
		pathfindingCourbe.computeNewPath(new Vec2<ReadOnly>(-1000, 200), true);
		ArrayList<LocomotionArc> trajet = chemin.get();
		for(LocomotionArc v : trajet)
		{
			log.debug(v);
		}
    }

}
