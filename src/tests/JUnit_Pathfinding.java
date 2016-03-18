package tests;

import obstacles.types.ObstacleRectangular;

import org.junit.Before;
import org.junit.Test;

import container.ServiceNames;
import pathfinding.CheminPathfinding;
import pathfinding.VitesseCourbure;
import pathfinding.astarCourbe.AStarCourbe;
import pathfinding.astarCourbe.ArcCourbe;
import pathfinding.astarCourbe.ClothoidesComputer;
import pathfinding.dstarlite.DStarLite;
import pathfinding.dstarlite.GridSpace;
import permissions.ReadOnly;
import robot.DirectionStrategy;
import robot.RobotReal;
import tests.graphicLib.Fenetre;
import utils.Config;
import utils.Sleep;
import utils.Vec2;

/**
 * Tests unitaires de la recherche de chemin.
 * @author pf
 *
 */

public class JUnit_Pathfinding extends JUnit_Test {

	private DStarLite pathfinding;
	private AStarCourbe pathfindingCourbe;
	private CheminPathfinding chemin;
	private RobotReal robot;
	private GridSpace gridspace;
	
	@Before
    public void setUp() throws Exception {
        super.setUp();
        pathfinding = (DStarLite) container.getService(ServiceNames.D_STAR_LITE);
        pathfindingCourbe = (AStarCourbe) container.getService(ServiceNames.A_STAR_COURBE_DYNAMIQUE);
        chemin = (CheminPathfinding) container.getService(ServiceNames.CHEMIN_PATHFINDING);
        robot = (RobotReal) container.getService(ServiceNames.ROBOT_REAL);
        gridspace = (GridSpace) container.getService(ServiceNames.GRID_SPACE);
	}

	
	@Test
    public void test_clotho() throws Exception
    {
		ClothoidesComputer clotho = (ClothoidesComputer) container.getService(ServiceNames.CLOTHOIDES_COMPUTER);
		int nbArc = 3;
		ArcCourbe arc[] = new ArcCourbe[nbArc];
		for(int i = 0; i < nbArc; i++)
			arc[i] = new ArcCourbe();
/*
		clotho.getTrajectoire(new Vec2<ReadOnly>(0, 1000), 0, 0, VitesseCourbure.GAUCHE_LENTEMENT, arc);
		
		if(Config.graphicObstacles)
			for(int i = 0; i < arc.vitesseCourbure.nbPoints; i++)
			{
				System.out.println(arc.arcselems[i].point);
				Fenetre.getInstance().addObstacleEnBiais(new ObstacleRectangular(arc.arcselems[i].point.getReadOnly(), 10, 10, 0));
			}
*/
		clotho.getTrajectoire(new Vec2<ReadOnly>(0, 1000), 0, 0, VitesseCourbure.DROITE_4, arc[0]);
		clotho.getTrajectoire(arc[0], VitesseCourbure.DROITE_4, arc[1]);
		clotho.getTrajectoire(arc[1], VitesseCourbure.DROITE_4, arc[2]);
//		clotho.getTrajectoire(arc[2], VitesseCourbure.GAUCHE_1, arc[3]);
//		clotho.getTrajectoire(arc[3], VitesseCourbure.GAUCHE_3, arc[4]);
//		clotho.getTrajectoire(arc[4], VitesseCourbure.DROITE_3, arc[5]);
//		clotho.getTrajectoire(arc[5], VitesseCourbure.GAUCHE_4, arc[6]);
//		clotho.getTrajectoire(arc[6], VitesseCourbure.GAUCHE_4, arc[7]);
//		clotho.getTrajectoire(arc[7], VitesseCourbure.GAUCHE_4, arc[8]);
//		clotho.getTrajectoire(arc[8], VitesseCourbure.GAUCHE_4, arc[9]);
		
		if(Config.graphicObstacles)
			for(int a = 0; a < nbArc; a++)			
				for(int i = 0; i < ClothoidesComputer.NB_POINTS; i++)
				{
					Sleep.sleep(100);
					System.out.println(arc[a].arcselems[i].point+" "+arc[a].arcselems[i].thetaDepart);
					Fenetre.getInstance().addObstacleEnBiais(new ObstacleRectangular(arc[a].arcselems[i].point.getReadOnly(), 10, 10, 0));
				}

		
    }
	
	@Test
    public void test_chemin_dstarlite() throws Exception
    {
		gridspace.addObstacle(new Vec2<ReadOnly>(200, 800), false);
		pathfinding.computeNewPath(new Vec2<ReadOnly>(-1000, 200), new Vec2<ReadOnly>(1200, 1200));
		pathfinding.itineraireBrut();		
		Sleep.sleep(500);
		log.debug("RECALCUL");
		gridspace.addObstacle(new Vec2<ReadOnly>(600, 1300), false);
		pathfinding.updatePath(new Vec2<ReadOnly>(600,1300));
		pathfinding.itineraireBrut();
		Sleep.sleep(4000);
		pathfinding.updatePath(new Vec2<ReadOnly>(-800,1300));
		pathfinding.itineraireBrut();
		log.debug("RECALCUL");
    }

	@Test
    public void test_chemin_thetastar() throws Exception
    {
		robot.setPositionOrientationCourbureDirection(new Vec2<ReadOnly>(-1000, 200), 0, 0, true);
		long avant = System.currentTimeMillis();
		for(int i = 0; i < 10000; i++)
//		pathfindingCourbe.computeNewPath(new Vec2<ReadOnly>(1000, 400), true, DirectionStrategy.FASTEST);
		log.debug("Durée d'une recherche : "+(System.currentTimeMillis() - avant)/10000.+" ms");
    }

}
