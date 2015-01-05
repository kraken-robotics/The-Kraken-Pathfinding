package tests;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import container.ServiceNames;
import astar.AStar;
import astar.arc.Decision;
import astar.arc.PathfindingNodes;
import astar.arcmanager.PathfindingArcManager;
import astar.arcmanager.StrategyArcManager;
import robot.RobotChrono;
import robot.RobotReal;
import scripts.ScriptManager;
import scripts.ScriptAnticipableNames;
import strategie.GameState;
import table.ObstacleManager;
import tests.graphicLib.Fenetre;
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
	private AStar<PathfindingArcManager, PathfindingNodes> pathfinding;
	private GameState<RobotChrono> state_chrono;
	private GameState<RobotReal> state;
	private AStar<StrategyArcManager, Decision> strategic_astar;
	private ScriptManager scriptmanager;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
        pathfinding = (AStar<PathfindingArcManager, PathfindingNodes>) container.getService(ServiceNames.A_STAR_PATHFINDING);
		obstaclemanager = (ObstacleManager) container.getService(ServiceNames.OBSTACLE_MANAGER);
		state = (GameState<RobotReal>)container.getService(ServiceNames.REAL_GAME_STATE);
		strategic_astar = (AStar<StrategyArcManager, Decision>)container.getService(ServiceNames.A_STAR_STRATEGY);
		scriptmanager = (ScriptManager) container.getService(ServiceNames.SCRIPT_MANAGER);
		fenetre = new Fenetre();
		fenetre.setDilatationObstacle(obstaclemanager.getDilatationObstacle());
		for(PathfindingNodes n : PathfindingNodes.values())
		{
			fenetre.addPoint(n.getCoordonnees());
			for(PathfindingNodes m : PathfindingNodes.values())
				if(!obstaclemanager.obstacle_fixe_dans_segment_pathfinding(n.getCoordonnees(), m.getCoordonnees()))
						fenetre.addSegment(m.getCoordonnees(), n.getCoordonnees());
		}
		updateAffichage();
		fenetre.showOnFrame();
	}
	
	public void updateAffichage()
	{
		fenetre.setGameElement(obstaclemanager.getListGameElement());
		fenetre.setObstaclesFixes(obstaclemanager.getListObstaclesFixes());
		// TODO: affiche aussi les obstacles périmés
		fenetre.setObstaclesMobiles(obstaclemanager.getListObstaclesMobiles());		
	}

	@Test
    public void test_verification_humaine() throws Exception
    {
		Random randomgenerator = new Random();
		for(int k = 0; k < 10; k++)
		{
			PathfindingNodes i = PathfindingNodes.values()[randomgenerator.nextInt(PathfindingNodes.values().length)];
			PathfindingNodes j = PathfindingNodes.values()[randomgenerator.nextInt(PathfindingNodes.values().length)];
			log.debug("Recherche chemin entre "+i+" et "+j, this);
			Vec2 entree = i.getCoordonnees().plusNewVector(new Vec2(randomgenerator.nextInt(100)-50, randomgenerator.nextInt(100)-50));
			config.setDateDebutMatch(); // afin d'avoir toujours une haute précision
			state_chrono = state.cloneGameState();
			double orientation_initiale = state_chrono.robot.getOrientation();
			state_chrono.robot.setPosition(entree);
			ArrayList<PathfindingNodes> chemin = pathfinding.computePath(state_chrono, j, true);
    		ArrayList<Vec2> cheminVec2 = new ArrayList<Vec2>();
    		cheminVec2.add(entree);
    		for(PathfindingNodes n: chemin)
    		{
    			log.debug(n, this);
    			cheminVec2.add(n.getCoordonnees());
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
		ArrayList<PathfindingNodes> cheminDepart = new ArrayList<PathfindingNodes>();
		cheminDepart.add(PathfindingNodes.POINT_DEPART);
    	Decision decision = new Decision(cheminDepart, ScriptAnticipableNames.SortieZoneDepart, 0);
    	config.setDateDebutMatch();
    	GameState<RobotChrono> chronostate = state.cloneGameState();
		ArrayList<Decision> decisions = strategic_astar.computeStrategyAfter(chronostate, decision, 10000);
		Vec2 position_precedente = PathfindingNodes.SORTIE_ZONE_DEPART.getCoordonnees();
		for(Decision d: decisions)
		{
//			log.debug(d, this);
    		ArrayList<Vec2> cheminVec2 = new ArrayList<Vec2>();
    		cheminVec2.add(position_precedente);
    		for(PathfindingNodes n: d.chemin)
    		{
//    			log.debug(n, this);
    			cheminVec2.add(n.getCoordonnees());
    		}
			fenetre.setPath(null, cheminVec2, Color.GRAY);
			fenetre.repaint();
			Sleep.sleep(100);
			ArrayList<Vec2> cheminVersSortie = new ArrayList<Vec2>();
			cheminVersSortie.add(scriptmanager.getScript(d.script_name).point_entree(d.version).getCoordonnees());
			cheminVersSortie.add(scriptmanager.getScript(d.script_name).point_sortie(d.version).getCoordonnees());
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
    		for(PathfindingNodes n: d.chemin)
    			cheminVec2.add(n.getCoordonnees());
			fenetre.setPath(null, cheminVec2, Color.GRAY);
			fenetre.repaint();
			Sleep.sleep(3000);
			ArrayList<Vec2> cheminVersSortie = new ArrayList<Vec2>();
			cheminVersSortie.add(scriptmanager.getScript(d.script_name).point_entree(d.version).getCoordonnees());
			cheminVersSortie.add(scriptmanager.getScript(d.script_name).point_sortie(d.version).getCoordonnees());
			position_precedente = scriptmanager.getScript(d.script_name).point_sortie(d.version).getCoordonnees();
			fenetre.setPath(null, cheminVersSortie, Color.RED);
			fenetre.repaint();
			Sleep.sleep(3000);
		}
    }

}
