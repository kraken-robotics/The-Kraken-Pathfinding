package tests;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import obstacles.ObstaclesMobilesIterator;
import obstacles.types.Obstacle;
import obstacles.types.ObstacleRectangular;
import obstacles.types.ObstacleRotationRobot;
import obstacles.types.ObstacleTrajectoireCourbe;
import obstacles.types.ObstaclesFixes;

import org.junit.Before;
import org.junit.Test;

import permissions.ReadOnly;
import permissions.ReadWrite;
import planification.astar.AStar;
import planification.astar.arc.Decision;
import planification.astar.arc.PathfindingNodes;
import planification.astar.arc.SegmentTrajectoireCourbe;
import planification.astar.arcmanager.PathfindingArcManager;
import planification.astar.arcmanager.StrategyArcManager;
import container.ServiceNames;
import robot.RobotChrono;
import robot.RobotReal;
import robot.Speed;
import scripts.ScriptManager;
import scripts.ScriptAnticipableNames;
import strategie.GameState;
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
	private ObstaclesMobilesIterator obstaclemanager;
	private AStar<PathfindingArcManager, SegmentTrajectoireCourbe> pathfinding;
	private GameState<RobotChrono,ReadWrite> state_chrono;
	private GameState<RobotReal,ReadWrite> state;
	private AStar<StrategyArcManager, Decision> strategic_astar;
	private ScriptManager scriptmanager;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
        pathfinding = (AStar<PathfindingArcManager, SegmentTrajectoireCourbe>) container.getService(ServiceNames.A_STAR_PATHFINDING);
		obstaclemanager = (ObstaclesMobilesIterator) container.getService(ServiceNames.OBSTACLE_MANAGER);
		state = (GameState<RobotReal,ReadWrite>)container.getService(ServiceNames.REAL_GAME_STATE);
		strategic_astar = (AStar<StrategyArcManager, Decision>)container.getService(ServiceNames.A_STAR_STRATEGY);
		scriptmanager = (ScriptManager) container.getService(ServiceNames.SCRIPT_MANAGER);
		fenetre = new Fenetre();
		fenetre.setDilatationObstacle(0);// TODO: test graphique
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

			log.debug("Recherche chemin entre "+i+" et "+j);
			Vec2<ReadOnly> entree = i.getCoordonnees().plusNewVector(new Vec2<ReadWrite>(randomgenerator.nextInt(100)-50, randomgenerator.nextInt(100)-50)).getReadOnly();
			config.setDateDebutMatch(); // afin d'avoir toujours une haute précision
			state_chrono = GameState.cloneGameState(state.getReadOnly());
			double orientation_initiale = GameState.getOrientation(state_chrono.getReadOnly());
			GameState.setPosition(state_chrono, entree);
			ArrayList<SegmentTrajectoireCourbe> chemin = pathfinding.computePath(state_chrono, j, true);
    		ArrayList<Vec2<ReadOnly>> cheminVec2 = new ArrayList<Vec2<ReadOnly>>();
    		cheminVec2.add(entree.getReadOnly());
    		for(SegmentTrajectoireCourbe n: chemin)
    		{
    			log.debug(n);
    			cheminVec2.utiliseActionneurs(n.objectifFinal.getCoordonnees());
    		}
    		fenetre.setPath(orientation_initiale, cheminVec2, Color.BLUE);
    		ArrayList<Vec2<ReadOnly>> direct = new ArrayList<Vec2<ReadOnly>>();
    		direct.add(entree.getReadOnly());
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
		cheminDepart.utiliseActionneurs(new SegmentTrajectoireCourbe(PathfindingNodes.POINT_DEPART));
    	Decision decision = new Decision(cheminDepart, ScriptAnticipableNames.SORTIE_ZONE_DEPART, PathfindingNodes.POINT_DEPART);
    	config.setDateDebutMatch();
    	GameState<RobotChrono,ReadOnly> chronostate = GameState.cloneGameState(state.getReadOnly()).getReadOnly();
		ArrayList<Decision> decisions = strategic_astar.computeStrategyAfter(chronostate, decision, 10000);
		Vec2<ReadOnly> position_precedente = PathfindingNodes.SORTIE_ZONE_DEPART.getCoordonnees();
		for(Decision d: decisions)
		{
//			log.debug(d, this);
    		ArrayList<Vec2<ReadOnly>> cheminVec2 = new ArrayList<Vec2<ReadOnly>>();
    		cheminVec2.add(position_precedente);
    		for(SegmentTrajectoireCourbe n: d.chemin)
    		{
//    			log.debug(n, this);
    			cheminVec2.utiliseActionneurs(n.objectifFinal.getCoordonnees());
    		}
			fenetre.setPath(null, cheminVec2, Color.GRAY);
			fenetre.repaint();
			Sleep.sleep(100);
			ArrayList<Vec2<ReadOnly>> cheminVersSortie = new ArrayList<Vec2<ReadOnly>>();
			cheminVersSortie.utiliseActionneurs(d.version.getCoordonnees());
			cheminVersSortie.utiliseActionneurs(d.version.getCoordonnees());
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
    	GameState<RobotChrono,ReadWrite> chronostate = GameState.cloneGameState(state.getReadOnly());
    	GameState.setPosition(chronostate, new Vec2<ReadOnly>(800, 1000));
    	GameState.setOrientation(chronostate, -Math.PI/2);
		ArrayList<Decision> decisions = strategic_astar.computeStrategyEmergency(chronostate.getReadOnly(), 10000);
		Vec2<ReadOnly> position_precedente = GameState.getPosition(chronostate.getReadOnly());
		for(Decision d: decisions)
		{
			log.debug(d);
    		ArrayList<Vec2<ReadOnly>> cheminVec2 = new ArrayList<Vec2<ReadOnly>>();
    		cheminVec2.add(position_precedente);
    		for(SegmentTrajectoireCourbe n: d.chemin)
    			cheminVec2.utiliseActionneurs(n.objectifFinal.getCoordonnees());
			fenetre.setPath(null, cheminVec2, Color.GRAY);
			fenetre.repaint();
			Sleep.sleep(3000);
			ArrayList<Vec2<ReadOnly>> cheminVersSortie = new ArrayList<Vec2<ReadOnly>>();
			cheminVersSortie.utiliseActionneurs(d.version.getCoordonnees());
			cheminVersSortie.utiliseActionneurs(d.version.getCoordonnees());
			position_precedente = scriptmanager.getScript(d.script_name).point_sortie(d.version).getCoordonnees();
			fenetre.setPath(null, cheminVersSortie, Color.RED);
			fenetre.repaint();
			Sleep.sleep(3000);
		}
    }

	@Test
    public void test_obstacle_rectangulaire() throws Exception
    {
		int largeur_robot = config.getInt(ConfigInfo.LARGEUR_ROBOT_AXE_GAUCHE_DROITE);
		int longueur_robot = config.getInt(ConfigInfo.LONGUEUR_ROBOT_AXE_AVANT_ARRIERE);
		int marge = 10;
		Vec2<ReadOnly> A = PathfindingNodes.CLAP_GAUCHE.getCoordonnees();
		Vec2<ReadOnly> B = PathfindingNodes.HAUT_GAUCHE.getCoordonnees();
		ObstacleRectangular<ReadOnly> o1 = new ObstacleRectangular<ReadOnly>(A.middleNewVector(B).getReadOnly(), (int)A.distance(B)+longueur_robot+2*marge, largeur_robot+2*marge, Math.atan2(B.y-A.y, B.x-A.x));
		ObstaclesFixes o = ObstaclesFixes.BANDE_1;
		ObstacleRectangular<ReadOnly> o2 = o.getObstacle();
    	ObstacleRectangular<ReadOnly> o3 = new ObstacleRectangular<ReadOnly>(new Vec2<ReadOnly>(1000,1000), 200, 200, Math.PI/8);
    	ObstacleRectangular<ReadOnly> o4 = new ObstacleRectangular<ReadOnly>(new Vec2<ReadOnly>(900, 1100), 50, 50, 0);
//    	ObstacleRectangular o3 = new ObstacleRectangular(new Vec2(1320, 250), longueur_robot, largeur_robot, Math.PI/6);
//    	ObstacleRectangular o4 = ObstaclesFixes.BORD_DROITE.getObstacle();
		log.debug("Collision ? "+o3.isColliding(o4));
		log.debug("Collision ? "+o1.isColliding(o2));
		fenetre.addObstacleEnBiais(o3);
		fenetre.addObstacleEnBiais(o4);
		fenetre.repaint();
		Sleep.sleep(5000);
    }
	
	@Test
    public void test_obstacle_pathfinding() throws Exception
    {
		int largeur_robot = config.getInt(ConfigInfo.LARGEUR_ROBOT_AXE_GAUCHE_DROITE);
		int longueur_robot = config.getInt(ConfigInfo.LONGUEUR_ROBOT_AXE_AVANT_ARRIERE);
		int marge = 10;
		Vec2<ReadOnly> A = PathfindingNodes.CLAP_GAUCHE.getCoordonnees();
		Vec2<ReadOnly> B = PathfindingNodes.HAUT_GAUCHE.getCoordonnees();
		ObstacleRectangular<ReadOnly> rectangle = new ObstacleRectangular<ReadOnly>(A.middleNewVector(B).getReadOnly(), (int)A.distance(B)+longueur_robot+2*marge, largeur_robot+2*marge, Math.atan2(B.y-A.y, B.x-A.x));
		fenetre.addObstacleEnBiais(rectangle);
		fenetre.repaint();
		ObstacleRectangular<ReadOnly> o = ObstaclesFixes.BANDE_1.getObstacle();
		log.debug("Collision avec "+Obstacle.getPosition(o.getTestOnly())+"? "+rectangle.isColliding(o));
		Sleep.sleep(3000);
    }
	
	@Test
    public void test_obstacle_rotation() throws Exception
    {
		GameState.setPosition(state, new Vec2<ReadOnly>(0, 1000));
		GameState.setOrientation(state, Math.PI/2);
    	double angleFinal = 0;
		ObstacleRectangular<ReadOnly>[] ombresRobot = new ObstacleRotationRobot<ReadOnly>(GameState.getPosition(state.getReadOnly()), GameState.getOrientation(state.getReadOnly()), angleFinal).getOmbresRobot();
		log.debug("Nb ombres: "+ombresRobot.length);
		for(ObstacleRectangular<ReadOnly> o: ombresRobot)
			fenetre.addObstacleEnBiais(o);
		fenetre.repaint();
		Sleep.sleep(3000);
    }

	@Test
    public void test_obstacle_trajectoire_courbe() throws Exception
    {
		Vec2<ReadOnly> position = new Vec2<ReadOnly>(0, 1000);
		PathfindingNodes fin = PathfindingNodes.COTE_MARCHE_DROITE;
		ObstacleTrajectoireCourbe<ReadOnly> obs = new ObstacleTrajectoireCourbe<ReadOnly>(fin, PathfindingNodes.BAS, new Vec2<ReadOnly>(Math.atan2(PathfindingNodes.BAS.getCoordonnees().y-position.y,PathfindingNodes.BAS.getCoordonnees().x-position.x)), Speed.BETWEEN_SCRIPTS);
		ObstacleRectangular<ReadOnly>[] ombresRobot = obs.getOmbresRobot();//new ObstacleTrajectoireCourbe(PathfindingNodes.DEVANT_DEPART_DROITE, PathfindingNodes.BAS, new Vec2(-Math.PI/2), Speed.BETWEEN_SCRIPTS).getOmbresRobot();
		log.debug("Nb ombres: "+ombresRobot.length);
		for(ObstacleRectangular<ReadOnly> o: ombresRobot)
			fenetre.addObstacleEnBiais(o);
		fenetre.repaint();
		Sleep.sleep(3000);
    }


}
