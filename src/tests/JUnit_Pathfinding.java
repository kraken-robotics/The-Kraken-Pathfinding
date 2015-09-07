package tests;

import java.util.ArrayList;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import container.ServiceNames;
import pathfinding.dstarlite.DStarLite;
import pathfinding.dstarlite.GridSpace;
import pathfinding.thetastar.CheminPathfinding;
import pathfinding.thetastar.LocomotionArc;
import pathfinding.thetastar.ThetaStar;
import permissions.ReadOnly;
import robot.DirectionStrategy;
import robot.RobotReal;
import utils.Vec2;

/**
 * Tests unitaires de la recherche de chemin.
 * @author pf
 *
 */

public class JUnit_Pathfinding extends JUnit_Test {

	private DStarLite pathfinding;
	private ThetaStar pathfindingCourbe;
	private CheminPathfinding chemin;
	private GridSpace gridspace;
	private RobotReal robot;
	
	@Before
    public void setUp() throws Exception {
        super.setUp();
        pathfinding = (DStarLite) container.getService(ServiceNames.D_STAR_LITE);
        pathfindingCourbe = (ThetaStar) container.getService(ServiceNames.THETA_STAR);
        chemin = (CheminPathfinding) container.getService(ServiceNames.CHEMIN_PATHFINDING);
        robot = (RobotReal) container.getService(ServiceNames.ROBOT_REAL);
        gridspace = (GridSpace) container.getService(ServiceNames.GRID_SPACE);
	}

	@Test
    public void test_chemin_dstarlite() throws Exception
    {
		pathfinding.computeNewPath(new Vec2<ReadOnly>(-1000, 200), gridspace.computeGridPoint(new Vec2<ReadOnly>(1200, 1500)));
		ArrayList<Vec2<ReadOnly>> trajet = pathfinding.itineraireBrut();
		for(Vec2<ReadOnly> v : trajet)
		{
			log.debug(v);
		}
    }

	@Test
    public void test_chemin_thetastar() throws Exception
    {
		robot.setPositionOrientationJava(new Vec2<ReadOnly>(-1000, 200), 0);
		long avant = System.currentTimeMillis();
		for(int i = 0; i < 10000; i++)
		pathfindingCourbe.computeNewPath(gridspace.computeGridPoint(new Vec2<ReadOnly>(1000, 400)), true, DirectionStrategy.FASTEST);
		log.debug("Durée d'une recherche : "+(System.currentTimeMillis() - avant)/10000.+" ms");
		LinkedList<LocomotionArc> trajet = chemin.get();
		for(LocomotionArc v : trajet)
		{
			log.debug(v);
		}
    }

}
