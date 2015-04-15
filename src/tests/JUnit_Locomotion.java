package tests;

import hook.Callback;
import hook.Executable;
import hook.Hook;
import hook.methods.ThrowsChangeDirection;
import hook.types.HookDemiPlan;

import java.util.ArrayList;

import obstacles.ObstacleRotationRobot;
import obstacles.ObstacleTrajectoireCourbe;
import obstacles.ObstaclesFixes;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import astar.arc.PathfindingNodes;
import astar.arc.SegmentTrajectoireCourbe;
import robot.DirectionStrategy;
import robot.Locomotion;
import robot.RobotReal;
import robot.Speed;
import strategie.GameState;
import utils.ConfigInfo;
import vec2.ReadOnly;
import vec2.Vec2;
import container.ServiceNames;
import enums.RobotColor;
import exceptions.UnableToMoveException;

/**
 * Tests unitaires des déplacements haut niveau
 * @author pf
 *
 */

public class JUnit_Locomotion extends JUnit_Test
{
	Locomotion locomotion;
	GameState<RobotReal> realstate;
	
	@SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        locomotion = (Locomotion) container.getService(ServiceNames.LOCOMOTION);
        realstate = (GameState<RobotReal>) container.getService(ServiceNames.REAL_GAME_STATE);
        locomotion.setPosition(new Vec2<ReadOnly>(0, 1000));
        locomotion.setOrientation(Math.PI/4);
    }
	
	@Test
	public void test_avancer() throws Exception
	{
		config.set(ConfigInfo.COULEUR, RobotColor.getCouleurSansSymetrie());
		locomotion.updateConfig();
        locomotion.setPosition(new Vec2<ReadOnly>(0, 1000));
        locomotion.setOrientation(Math.PI/4);
		locomotion.moveLengthwise(150, new ArrayList<Hook>(), false);
		locomotion.moveLengthwise(-150, new ArrayList<Hook>(), false);
	}

	@Test
	public void test_avancer2() throws Exception
	{
		config.set(ConfigInfo.COULEUR, RobotColor.getCouleurAvecSymetrie());
		locomotion.updateConfig();
        locomotion.setPosition(new Vec2<ReadOnly>(0, 1000));
        locomotion.setOrientation(Math.PI/4);
		locomotion.moveLengthwise(150, new ArrayList<Hook>(), false);
		locomotion.moveLengthwise(-150, new ArrayList<Hook>(), false);
	}

	@Test
	public void test_tourner() throws Exception
	{
		config.set(ConfigInfo.COULEUR, RobotColor.getCouleurSansSymetrie());
		locomotion.updateConfig();
        locomotion.setPosition(new Vec2<ReadOnly>(0, 1000));
        locomotion.setOrientation(Math.PI/4);
		locomotion.turn(Math.PI/2, new ArrayList<Hook>());
		locomotion.turn(Math.PI, new ArrayList<Hook>());
		locomotion.turn(3*Math.PI/2, new ArrayList<Hook>());
		locomotion.turn(0, new ArrayList<Hook>());
		config.set(ConfigInfo.COULEUR, RobotColor.getCouleurAvecSymetrie());
		locomotion.updateConfig();
        locomotion.setPosition(new Vec2<ReadOnly>(0, 1000));
        locomotion.setOrientation(Math.PI/4);
		locomotion.turn(Math.PI/2, new ArrayList<Hook>());
		locomotion.turn(Math.PI, new ArrayList<Hook>());
		locomotion.turn(3*Math.PI/2, new ArrayList<Hook>());
		locomotion.turn(0, new ArrayList<Hook>());
	}
	
	@Test(expected=UnableToMoveException.class)
	public void test_tourner_mur() throws Exception
	{
        locomotion.setOrientation(0);
        locomotion.setPosition(new Vec2<ReadOnly>(1320, 250));
        locomotion.turn(Math.PI/6, new ArrayList<Hook>());
	}

	@Test(expected=UnableToMoveException.class)
	public void test_avancer_mur() throws Exception
	{
        locomotion.setOrientation(0);
        locomotion.setPosition(new Vec2<ReadOnly>(1320, 250));
        locomotion.moveLengthwise(200, new ArrayList<Hook>(), false);
	}

	@Test
	public void test_suit_chemin() throws Exception
	{
		config.set(ConfigInfo.COULEUR, RobotColor.getCouleurAvecSymetrie());
		locomotion.updateConfig();
        locomotion.setPosition(new Vec2<ReadOnly>(0, 1000));
        locomotion.setOrientation(Math.PI/4);
		ArrayList<SegmentTrajectoireCourbe> chemin = new ArrayList<SegmentTrajectoireCourbe>();
		chemin.add(new SegmentTrajectoireCourbe(PathfindingNodes.BAS));
		chemin.add(new SegmentTrajectoireCourbe(PathfindingNodes.DEVANT_DEPART_GAUCHE));
		chemin.add(new SegmentTrajectoireCourbe(PathfindingNodes.COTE_MARCHE_GAUCHE));
		chemin.add(new SegmentTrajectoireCourbe(PathfindingNodes.DEVANT_DEPART_GAUCHE));
		
		locomotion.followPath(chemin, new HookDemiPlan(config, log, realstate), new ArrayList<Hook>(), DirectionStrategy.FASTEST);
		locomotion.followPath(chemin, new HookDemiPlan(config, log, realstate), new ArrayList<Hook>(), DirectionStrategy.FORCE_FORWARD_MOTION);
	}
	
	@Test
	public void test_suit_chemin_courbe() throws Exception
	{
		config.set(ConfigInfo.COULEUR, RobotColor.getCouleurSansSymetrie());
		locomotion.updateConfig();
        locomotion.setPosition(new Vec2<ReadOnly>(-200, 1200));
        locomotion.setOrientation(0);
		ArrayList<SegmentTrajectoireCourbe> chemin = new ArrayList<SegmentTrajectoireCourbe>();

		HookDemiPlan hookTrajectoireCourbe = new HookDemiPlan(config, log, realstate);
		Executable action = new ThrowsChangeDirection();
		hookTrajectoireCourbe.ajouter_callback(new Callback(action));

		locomotion.setRotationnalSpeed(Speed.BETWEEN_SCRIPTS);
		locomotion.setTranslationnalSpeed(Speed.BETWEEN_SCRIPTS);
//		PathfindingNodes fin = PathfindingNodes.COTE_MARCHE_GAUCHE;
		PathfindingNodes fin = PathfindingNodes.DEVANT_DEPART_DROITE;
		Vec2<ReadOnly> position = locomotion.getPosition();
		ObstacleTrajectoireCourbe obs = new ObstacleTrajectoireCourbe(fin, PathfindingNodes.BAS, new Vec2<ReadOnly>(Math.atan2(PathfindingNodes.BAS.getCoordonnees().y-position.y,PathfindingNodes.BAS.getCoordonnees().x-position.x)), Speed.BETWEEN_SCRIPTS);
		chemin.add(new SegmentTrajectoireCourbe(PathfindingNodes.BAS));
		chemin.add(obs.getSegment());
		chemin.add(new SegmentTrajectoireCourbe(PathfindingNodes.DEVANT_DEPART_GAUCHE));
		
//		locomotion.followPath(chemin, new HookDemiPlan(config, log, realstate), new ArrayList<Hook>(), DirectionStrategy.FASTEST);
		locomotion.followPath(chemin, hookTrajectoireCourbe, new ArrayList<Hook>(), DirectionStrategy.FASTEST);
	}

	
	@Test
	public void test_mur() throws Exception
	{
        locomotion.setPosition(new Vec2<ReadOnly>(-1200, 1000));
        locomotion.setOrientation(0);
		locomotion.moveLengthwise(-300, new ArrayList<Hook>(), true);
	}

	@Test(expected=UnableToMoveException.class)
	public void test_mur_exception() throws Exception
	{
        locomotion.setPosition(new Vec2<ReadOnly>(-1200, 1000));
        locomotion.setOrientation(0);
		locomotion.moveLengthwise(-300, new ArrayList<Hook>(), false);
	}

	@Test
	public void test_rotation_obstacle() throws Exception
	{
        locomotion.setOrientation(0);
        locomotion.setPosition(new Vec2<ReadOnly>(1320, 250));
        ObstacleRotationRobot r;
        r = new ObstacleRotationRobot(locomotion.getPosition(), locomotion.getOrientation(), 0);
        Assert.assertTrue(!r.isColliding(ObstaclesFixes.BORD_DROITE.getObstacle()));
        r = new ObstacleRotationRobot(locomotion.getPosition(), locomotion.getOrientation(), Math.PI/9);
        Assert.assertTrue(!r.isColliding(ObstaclesFixes.BORD_DROITE.getObstacle()));
        r = new ObstacleRotationRobot(locomotion.getPosition(), locomotion.getOrientation(), Math.PI/6);
        Assert.assertTrue(r.isColliding(ObstaclesFixes.BORD_DROITE.getObstacle()));
        r = new ObstacleRotationRobot(locomotion.getPosition(), locomotion.getOrientation(), Math.PI/2);
        Assert.assertTrue(r.isColliding(ObstaclesFixes.BORD_DROITE.getObstacle()));
        r = new ObstacleRotationRobot(locomotion.getPosition(), locomotion.getOrientation(), -Math.PI/9);
        Assert.assertTrue(!r.isColliding(ObstaclesFixes.BORD_DROITE.getObstacle()));
        r = new ObstacleRotationRobot(locomotion.getPosition(), locomotion.getOrientation(), -Math.PI/6);
        Assert.assertTrue(r.isColliding(ObstaclesFixes.BORD_DROITE.getObstacle()));
        r = new ObstacleRotationRobot(locomotion.getPosition(), locomotion.getOrientation(), -Math.PI/2);
        Assert.assertTrue(r.isColliding(ObstaclesFixes.BORD_DROITE.getObstacle()));
	}
	
}
