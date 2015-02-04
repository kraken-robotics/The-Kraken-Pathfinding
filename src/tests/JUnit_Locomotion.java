package tests;

import hook.Hook;
import hook.types.HookDemiPlan;

import java.util.ArrayList;

import obstacles.ObstacleRotationRobot;
import obstacles.ObstaclesFixes;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import astar.arc.PathfindingNodes;
import astar.arc.SegmentTrajectoireCourbe;
import robot.DirectionStrategy;
import robot.Locomotion;
import utils.ConfigInfo;
import utils.Vec2;
import container.ServiceNames;
import exceptions.UnableToMoveException;

/**
 * Tests unitaires des déplacements haut niveau
 * @author pf
 *
 */

public class JUnit_Locomotion extends JUnit_Test
{
	Locomotion locomotion;
	
	@Before
    public void setUp() throws Exception {
        super.setUp();
        locomotion = (Locomotion) container.getService(ServiceNames.LOCOMOTION);
        locomotion.setPosition(new Vec2(0, 1000));
        locomotion.setOrientation(0);
    }
	
	@Test
	public void test_avancer() throws Exception
	{
		config.set(ConfigInfo.COULEUR, "vert");
		locomotion.moveLengthwise(150, new ArrayList<Hook>(), false);
		locomotion.moveLengthwise(-150, new ArrayList<Hook>(), false);
		config.set(ConfigInfo.COULEUR, "jaune");
		locomotion.updateConfig();
		locomotion.moveLengthwise(150, new ArrayList<Hook>(), false);
		locomotion.moveLengthwise(-150, new ArrayList<Hook>(), false);
	}

	@Test
	public void test_tourner() throws Exception
	{
		config.set(ConfigInfo.COULEUR, "vert");
		locomotion.updateConfig();
		locomotion.turn(Math.PI/2, new ArrayList<Hook>());
		locomotion.turn(Math.PI, new ArrayList<Hook>());
		locomotion.turn(3*Math.PI/2, new ArrayList<Hook>());
		locomotion.turn(0, new ArrayList<Hook>());
		config.set(ConfigInfo.COULEUR, "jaune");
		locomotion.updateConfig();
		locomotion.turn(Math.PI/2, new ArrayList<Hook>());
		locomotion.turn(Math.PI, new ArrayList<Hook>());
		locomotion.turn(3*Math.PI/2, new ArrayList<Hook>());
		locomotion.turn(0, new ArrayList<Hook>());
	}
	
	@Test(expected=UnableToMoveException.class)
	public void test_tourner_mur() throws Exception
	{
        locomotion.setOrientation(0);
        locomotion.setPosition(new Vec2(1320, 250));
        locomotion.turn(Math.PI/6, new ArrayList<Hook>());
	}

	@Test(expected=UnableToMoveException.class)
	public void test_avancer_mur() throws Exception
	{
        locomotion.setOrientation(0);
        locomotion.setPosition(new Vec2(1320, 250));
        locomotion.moveLengthwise(200, new ArrayList<Hook>(), false);
	}

	@Test
	public void test_suit_chemin() throws Exception
	{
		config.set(ConfigInfo.COULEUR, "jaune");
		locomotion.updateConfig();
		ArrayList<SegmentTrajectoireCourbe> chemin = new ArrayList<SegmentTrajectoireCourbe>();
		chemin.add(new SegmentTrajectoireCourbe(PathfindingNodes.BAS));
		chemin.add(new SegmentTrajectoireCourbe(PathfindingNodes.DEVANT_DEPART_GAUCHE));
		chemin.add(new SegmentTrajectoireCourbe(PathfindingNodes.COTE_MARCHE_GAUCHE));
		chemin.add(new SegmentTrajectoireCourbe(PathfindingNodes.DEVANT_DEPART_GAUCHE));
		
		locomotion.followPath(chemin, new HookDemiPlan(config, log, null), new ArrayList<Hook>(), DirectionStrategy.FASTEST);
		locomotion.followPath(chemin, new HookDemiPlan(config, log, null), new ArrayList<Hook>(), DirectionStrategy.FORCE_FORWARD_MOTION);
	}
	
	@Test
	public void test_mur() throws Exception
	{
        locomotion.setPosition(new Vec2(-1200, 1000));
        locomotion.setOrientation(0);
		locomotion.moveLengthwise(-300, new ArrayList<Hook>(), true);
	}

	@Test(expected=UnableToMoveException.class)
	public void test_mur_exception() throws Exception
	{
        locomotion.setPosition(new Vec2(-1200, 1000));
        locomotion.setOrientation(0);
		locomotion.moveLengthwise(-300, new ArrayList<Hook>(), false);
	}

	@Test
	public void test_rotation_obstacle() throws Exception
	{
        locomotion.setOrientation(0);
        locomotion.setPosition(new Vec2(1320, 250));
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
