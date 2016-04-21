package tests;

import obstacles.types.ObstacleRectangular;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import container.ServiceNames;
import pathfinding.CheminPathfinding;
import pathfinding.CheminPlanif;
import pathfinding.ChronoGameState;
import pathfinding.RealGameState;
import pathfinding.VitesseCourbure;
import pathfinding.astarCourbe.AStarCourbe;
import pathfinding.astarCourbe.AStarCourbeDynamique;
import pathfinding.astarCourbe.AStarCourbePlanif;
import pathfinding.astarCourbe.ArcCourbe;
import pathfinding.astarCourbe.ClothoidesComputer;
import pathfinding.dstarlite.DStarLite;
import pathfinding.dstarlite.GridSpace;
import robot.Cinematique;
import robot.DirectionStrategy;
import robot.RobotReal;
import robot.Speed;
import tests.graphicLib.Fenetre;
import utils.Config;
import utils.Sleep;
import utils.Vec2;
import utils.permissions.ReadOnly;

/**
 * Tests unitaires de la recherche de chemin.
 * @author pf
 *
 */

public class JUnit_Pathfinding extends JUnit_Test {

	private DStarLite pathfinding;
	private AStarCourbeDynamique pathfindingCourbeDyn;
	private AStarCourbePlanif pathfindingCourbePlanif;
	private CheminPathfinding chemin;
	private CheminPlanif cheminPlanif;
	private RobotReal robot;
	private GridSpace gridspace;
	private RealGameState state;
	
	@Before
    public void setUp() throws Exception {
        super.setUp();
        pathfinding = (DStarLite) container.getService(ServiceNames.D_STAR_LITE);
        pathfindingCourbeDyn = (AStarCourbeDynamique) container.getService(ServiceNames.A_STAR_COURBE_DYNAMIQUE);
        pathfindingCourbePlanif = (AStarCourbePlanif) container.getService(ServiceNames.A_STAR_COURBE_PLANIFICATION);
        chemin = (CheminPathfinding) container.getService(ServiceNames.CHEMIN_PATHFINDING);
        cheminPlanif = (CheminPlanif) container.getService(ServiceNames.CHEMIN_PLANIF);
        robot = (RobotReal) container.getService(ServiceNames.ROBOT_REAL);
        state = (RealGameState) container.getService(ServiceNames.REAL_GAME_STATE);
        gridspace = (GridSpace) container.getService(ServiceNames.GRID_SPACE);
	}

	@Test
    public void test_pathfinding_planif() throws Exception
    {
		Cinematique arrivee = new Cinematique(1000, 500, 0, true, 0, 0, 0);
		log.debug(cheminPlanif.size());
		pathfindingCourbePlanif.computeNewPath(state.cloneGameState(), arrivee, true, DirectionStrategy.FASTEST);
		log.debug(cheminPlanif.size());
		
		while(!cheminPlanif.isEmpty())
		{
			ArcCourbe arc = cheminPlanif.poll();
			System.out.println("arc avec "+arc.arcselems[0]);
			for(int i = 0; i < ClothoidesComputer.NB_POINTS; i++)
			{
				System.out.println(arc.arcselems[i].getPosition()+" "+arc.arcselems[i].courbure);
				if(Config.graphicObstacles)
				{
					Sleep.sleep(100);
					Fenetre.getInstance().addObstacleEnBiais(new ObstacleRectangular(arc.arcselems[i].getPosition(), 10, 10, 0));
				}
			}
		}
    }
	
	@Test
    public void test_clotho() throws Exception
    {
		ClothoidesComputer clotho = (ClothoidesComputer) container.getService(ServiceNames.CLOTHOIDES_COMPUTER);
		int nbArc = 16;
		ArcCourbe arc[] = new ArcCourbe[nbArc];
		for(int i = 0; i < nbArc; i++)
			arc[i] = new ArcCourbe();

		Cinematique c = new Cinematique(0, 1000, Math.PI/2, true, 0, 0, 0);
		
		clotho.getTrajectoire(c, VitesseCourbure.COURBURE_IDENTIQUE, Speed.STANDARD, arc[0]);
		clotho.getTrajectoire(arc[0], VitesseCourbure.GAUCHE_3, Speed.STANDARD, arc[1]);
		clotho.getTrajectoire(arc[1], VitesseCourbure.COURBURE_IDENTIQUE, Speed.STANDARD, arc[2]);
		clotho.getTrajectoire(arc[2], VitesseCourbure.GAUCHE_1, Speed.STANDARD, arc[3]);
		clotho.getTrajectoire(arc[3], VitesseCourbure.COURBURE_IDENTIQUE, Speed.STANDARD, arc[4]);
		clotho.getTrajectoire(arc[4], VitesseCourbure.COURBURE_IDENTIQUE, Speed.STANDARD, arc[5]);
		clotho.getTrajectoire(arc[5], VitesseCourbure.GAUCHE_3_REBROUSSE, Speed.STANDARD, arc[6]);
		clotho.getTrajectoire(arc[6], VitesseCourbure.GAUCHE_2, Speed.STANDARD, arc[7]);
		clotho.getTrajectoire(arc[7], VitesseCourbure.GAUCHE_3, Speed.STANDARD, arc[8]);
		clotho.getTrajectoire(arc[8], VitesseCourbure.DROITE_3_REBROUSSE, Speed.STANDARD, arc[9]);
		clotho.getTrajectoire(arc[9], VitesseCourbure.DROITE_1, Speed.STANDARD, arc[10]);
		clotho.getTrajectoire(arc[10], VitesseCourbure.DROITE_1, Speed.STANDARD, arc[11]);
		clotho.getTrajectoire(arc[11], VitesseCourbure.DROITE_1, Speed.STANDARD, arc[12]);
		clotho.getTrajectoire(arc[12], VitesseCourbure.DROITE_1, Speed.STANDARD, arc[13]);
		clotho.getTrajectoire(arc[13], VitesseCourbure.DROITE_1, Speed.STANDARD, arc[14]);
		clotho.getTrajectoire(arc[14], VitesseCourbure.GAUCHE_3, Speed.STANDARD, arc[15]);
	
		for(int a = 0; a < nbArc; a++)	
		{
			System.out.println("arc "+a+" avec "+arc[a].arcselems[0]);
			for(int i = 0; i < ClothoidesComputer.NB_POINTS; i++)
			{
/*				if(i > 0)
					System.out.println(arc[a].arcselems[i-1].point.distance(arc[a].arcselems[i].point));
				else if(a > 0)
					System.out.println(arc[a-1].arcselems[ClothoidesComputer.NB_POINTS - 1].point.distance(arc[a].arcselems[0].point));
	*/				
				System.out.println(arc[a].arcselems[i].getPosition()+" "+arc[a].arcselems[i].courbure);
				if(Config.graphicObstacles)
				{
					Sleep.sleep(100);
					Fenetre.getInstance().addObstacleEnBiais(new ObstacleRectangular(arc[a].arcselems[i].getPosition(), 10, 10, 0));
				}
			}
			if(a == 0)
			{
				Assert.assertEquals(arc[0].arcselems[ClothoidesComputer.NB_POINTS - 1].getPositionEcriture().x, 0);
				Assert.assertEquals(arc[0].arcselems[ClothoidesComputer.NB_POINTS - 1].getPositionEcriture().y, 1000+(int)ClothoidesComputer.DISTANCE_ARC_COURBE);
			}
/*			else if(arc[a].arcselems[0].enMarcheAvant != arc[a-1].arcselems[0].enMarcheAvant)
				Assert.assertEquals(arc[a].vitesseCourbure.vitesse / 1000. * ClothoidesComputer.DISTANCE_ARC_COURBE, arc[a].arcselems[ClothoidesComputer.NB_POINTS-1].courbure, 0.1);
			else if(a > 0)
				Assert.assertEquals(arc[a].vitesseCourbure.vitesse / 1000. * ClothoidesComputer.DISTANCE_ARC_COURBE + arc[a-1].arcselems[ClothoidesComputer.NB_POINTS-1].courbure, arc[a].arcselems[ClothoidesComputer.NB_POINTS-1].courbure, 0.1);
*/		}
		
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

}
