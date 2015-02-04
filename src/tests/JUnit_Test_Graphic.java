package tests;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import obstacles.ObstacleTrajectoireCourbe;
import obstacles.ObstacleRectangular;
import obstacles.ObstacleRotationRobot;
import obstacles.ObstaclesFixes;

import org.junit.Before;
import org.junit.Test;

import container.ServiceNames;
import astar.AStar;
import astar.arc.Decision;
import astar.arc.PathfindingNodes;
import astar.arc.SegmentTrajectoireCourbe;
import astar.arcmanager.PathfindingArcManager;
import astar.arcmanager.StrategyArcManager;
import robot.RobotChrono;
import robot.RobotReal;
import robot.Speed;
import scripts.ScriptManager;
import scripts.ScriptAnticipableNames;
import strategie.GameState;
import table.ObstacleManager;
import tests.graphicLib.Fenetre;
import utils.ConfigInfo;
import utils.Sleep;
import utils.Vec2;

/**
 * Tests unitaires disposant d'une interface graphique.
 * Utilisé pour la vérification humaine.
 * @author pf
 *
 */

public class JUnit_Test_Graphic extends JUnit_Test {

	private Fenetre fenetre;
	private ObstacleManager obstaclemanager;
	private AStar<PathfindingArcManager, SegmentTrajectoireCourbe> pathfinding;
	private GameState<RobotChrono> state_chrono;
	private GameState<RobotReal> state;
	private AStar<StrategyArcManager, Decision> strategic_astar;
	private ScriptManager scriptmanager;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
        pathfinding = (AStar<PathfindingArcManager, SegmentTrajectoireCourbe>) container.getService(ServiceNames.A_STAR_PATHFINDING);
		obstaclemanager = (ObstacleManager) container.getService(ServiceNames.OBSTACLE_MANAGER);
		state = (GameState<RobotReal>)container.getService(ServiceNames.REAL_GAME_STATE);
		strategic_astar = (AStar<StrategyArcManager, Decision>)container.getService(ServiceNames.A_STAR_STRATEGY);
		scriptmanager = (ScriptManager) container.getService(ServiceNames.SCRIPT_MANAGER);
		fenetre = new Fenetre();
		fenetre.setDilatationObstacle(0);// TODO: refaire ça
		for(PathfindingNodes n : PathfindingNodes.values())
		{
			fenetre.addPoint(n.getCoordonnees());
			for(PathfindingNodes m : PathfindingNodes.values())
				if(!obstaclemanager.obstacleFixeDansSegmentPathfinding(n.getCoordonnees(), m.getCoordonnees()))
						fenetre.addSegment(m.getCoordonnees(), n.getCoordonnees());
		}
		updateAffichage();
		fenetre.showOnFrame();
	}
	
	public void updateAffichage()
	{
		fenetre.setObstaclesMobiles(obstaclemanager.getListObstaclesMobiles(), obstaclemanager.getFirstNotDead());
	}

	@Test
    public void test_pathfinding_verification_humaine() throws Exception
    {
		Random randomgenerator = new Random();
		for(int k = 0; k < 10; k++)
		{
			PathfindingNodes i = PathfindingNodes.values()[randomgenerator.nextInt(PathfindingNodes.values().length)];
			PathfindingNodes j = PathfindingNodes.values()[randomgenerator.nextInt(PathfindingNodes.values().length)];
//			i = PathfindingNodes.BAS_GAUCHE;
//			j = PathfindingNodes.COTE_MARCHE_GAUCHE;

			log.debug("Recherche chemin entre "+i+" et "+j, this);
			Vec2 entree = i.getCoordonnees().plusNewVector(new Vec2(randomgenerator.nextInt(100)-50, randomgenerator.nextInt(100)-50));
			config.setDateDebutMatch(); // afin d'avoir toujours une haute précision
			state_chrono = state.cloneGameState();
			double orientation_initiale = state_chrono.robot.getOrientation();
			state_chrono.robot.setPosition(entree);
			ArrayList<SegmentTrajectoireCourbe> chemin = pathfinding.computePath(state_chrono, j, true);
    		ArrayList<Vec2> cheminVec2 = new ArrayList<Vec2>();
    		cheminVec2.add(entree);
    		for(SegmentTrajectoireCourbe n: chemin)
    		{
    			log.debug(n, this);
    			cheminVec2.add(n.objectifFinal.getCoordonnees());
    		}
    		fenetre.setPath(orientation_initiale, cheminVec2, Color.BLUE);
    		ArrayList<Vec2> direct = new ArrayList<Vec2>();
    		direct.add(entree);
    		direct.add(j.getCoordonnees());
    		fenetre.setPath(orientation_initiale, direct, Color.ORANGE);
    		fenetre.repaint();
    		Sleep.sleep(2000);
    		fenetre.resetPath();
		}
    }

    @Test
    public void test_strategy_verification_humaine() throws Exception
    {
		ArrayList<SegmentTrajectoireCourbe> cheminDepart = new ArrayList<SegmentTrajectoireCourbe>();
		cheminDepart.add(new SegmentTrajectoireCourbe(PathfindingNodes.POINT_DEPART));
    	Decision decision = new Decision(cheminDepart, ScriptAnticipableNames.SORTIE_ZONE_DEPART, PathfindingNodes.POINT_DEPART);
    	config.setDateDebutMatch();
    	GameState<RobotChrono> chronostate = state.cloneGameState();
		ArrayList<Decision> decisions = strategic_astar.computeStrategyAfter(chronostate, decision, 10000);
		Vec2 position_precedente = PathfindingNodes.SORTIE_ZONE_DEPART.getCoordonnees();
		for(Decision d: decisions)
		{
//			log.debug(d, this);
    		ArrayList<Vec2> cheminVec2 = new ArrayList<Vec2>();
    		cheminVec2.add(position_precedente);
    		for(SegmentTrajectoireCourbe n: d.chemin)
    		{
//    			log.debug(n, this);
    			cheminVec2.add(n.objectifFinal.getCoordonnees());
    		}
			fenetre.setPath(null, cheminVec2, Color.GRAY);
			fenetre.repaint();
			Sleep.sleep(100);
			ArrayList<Vec2> cheminVersSortie = new ArrayList<Vec2>();
			cheminVersSortie.add(d.version.getCoordonnees());
			cheminVersSortie.add(d.version.getCoordonnees());
			position_precedente = scriptmanager.getScript(d.script_name).point_sortie(d.version).getCoordonnees();
			fenetre.setPath(null, cheminVersSortie, Color.RED);
			fenetre.repaint();
			Sleep.sleep(100);
	//		log.debug(d, this);
		}
    }

    @Test
    public void test_strategy_emergency_verification_humaine() throws Exception
    {
    	config.setDateDebutMatch();
    	GameState<RobotChrono> chronostate = state.cloneGameState();
    	chronostate.robot.setPosition(new Vec2(800, 1000));
    	chronostate.robot.setOrientation(-Math.PI/2);
		ArrayList<Decision> decisions = strategic_astar.computeStrategyEmergency(chronostate, 10000);
		Vec2 position_precedente = chronostate.robot.getPosition();
		for(Decision d: decisions)
		{
			log.debug(d, this);
    		ArrayList<Vec2> cheminVec2 = new ArrayList<Vec2>();
    		cheminVec2.add(position_precedente);
    		for(SegmentTrajectoireCourbe n: d.chemin)
    			cheminVec2.add(n.objectifFinal.getCoordonnees());
			fenetre.setPath(null, cheminVec2, Color.GRAY);
			fenetre.repaint();
			Sleep.sleep(3000);
			ArrayList<Vec2> cheminVersSortie = new ArrayList<Vec2>();
			cheminVersSortie.add(d.version.getCoordonnees());
			cheminVersSortie.add(d.version.getCoordonnees());
			position_precedente = scriptmanager.getScript(d.script_name).point_sortie(d.version).getCoordonnees();
			fenetre.setPath(null, cheminVersSortie, Color.RED);
			fenetre.repaint();
			Sleep.sleep(3000);
		}
    }

	@Test
    public void test_obstacle_rectangulaire() throws Exception
    {
		int largeur_robot = config.getInt(ConfigInfo.LARGEUR_ROBOT);
		int longueur_robot = config.getInt(ConfigInfo.LONGUEUR_ROBOT);
		int marge = 10;
		Vec2 A = PathfindingNodes.CLAP_GAUCHE.getCoordonnees();
		Vec2 B = PathfindingNodes.HAUT_GAUCHE.getCoordonnees();
		ObstacleRectangular o1 = new ObstacleRectangular(A.middleNewVector(B), (int)A.distance(B)+longueur_robot+2*marge, largeur_robot+2*marge, Math.atan2(B.y-A.y, B.x-A.x));
		ObstaclesFixes o = ObstaclesFixes.BANDE_1;
		ObstacleRectangular o2 = o.getObstacle();
    	ObstacleRectangular o3 = new ObstacleRectangular(new Vec2(1000,1000), 200, 200, Math.PI/8);
    	ObstacleRectangular o4 = new ObstacleRectangular(new Vec2(900, 1100), 50, 50, 0);
//    	ObstacleRectangular o3 = new ObstacleRectangular(new Vec2(1320, 250), longueur_robot, largeur_robot, Math.PI/6);
//    	ObstacleRectangular o4 = ObstaclesFixes.BORD_DROITE.getObstacle();
		log.debug("Collision ? "+o3.isColliding(o4), this);
		log.debug("Collision ? "+o1.isColliding(o2), this);
		fenetre.addObstacleEnBiais(o3);
		fenetre.addObstacleEnBiais(o4);
		fenetre.repaint();
		Sleep.sleep(5000);
    }
	
	@Test
    public void test_obstacle_pathfinding() throws Exception
    {
		int largeur_robot = config.getInt(ConfigInfo.LARGEUR_ROBOT);
		int longueur_robot = config.getInt(ConfigInfo.LONGUEUR_ROBOT);
		int marge = 10;
		Vec2 A = PathfindingNodes.CLAP_GAUCHE.getCoordonnees();
		Vec2 B = PathfindingNodes.HAUT_GAUCHE.getCoordonnees();
		ObstacleRectangular rectangle = new ObstacleRectangular(A.middleNewVector(B), (int)A.distance(B)+longueur_robot+2*marge, largeur_robot+2*marge, Math.atan2(B.y-A.y, B.x-A.x));
		fenetre.addObstacleEnBiais(rectangle);
		fenetre.repaint();
		ObstacleRectangular o = ObstaclesFixes.BANDE_1.getObstacle();
		log.debug("Collision avec "+o.getPosition()+"? "+rectangle.isColliding(o), this);
		Sleep.sleep(3000);
    }
	
	@Test
    public void test_obstacle_rotation() throws Exception
    {
    	state.robot.setPosition(new Vec2(0, 1000));
    	state.robot.setOrientation(Math.PI/2);
    	double angleFinal = 0;
		ObstacleRectangular[] ombresRobot = new ObstacleRotationRobot(state.robot.getPosition(), state.robot.getOrientation(), angleFinal).getOmbresRobot();
		log.debug("Nb ombres: "+ombresRobot.length, this);
		for(ObstacleRectangular o: ombresRobot)
			fenetre.addObstacleEnBiais(o);
		fenetre.repaint();
		Sleep.sleep(3000);
    }

	@Test
    public void test_obstacle_trajectoire_courbe() throws Exception
    {
		Vec2 position = new Vec2(0, 1000);
		PathfindingNodes fin = PathfindingNodes.COTE_MARCHE_DROITE;
		ObstacleTrajectoireCourbe obs = new ObstacleTrajectoireCourbe(fin, PathfindingNodes.BAS, new Vec2(Math.atan2(PathfindingNodes.BAS.getCoordonnees().y-position.y,PathfindingNodes.BAS.getCoordonnees().x-position.x)), Speed.BETWEEN_SCRIPTS);
		ObstacleRectangular[] ombresRobot = obs.getOmbresRobot();//new ObstacleTrajectoireCourbe(PathfindingNodes.DEVANT_DEPART_DROITE, PathfindingNodes.BAS, new Vec2(-Math.PI/2), Speed.BETWEEN_SCRIPTS).getOmbresRobot();
		log.debug("Nb ombres: "+ombresRobot.length, this);
		for(ObstacleRectangular o: ombresRobot)
			fenetre.addObstacleEnBiais(o);
		fenetre.repaint();
		Sleep.sleep(3000);
    }


}
