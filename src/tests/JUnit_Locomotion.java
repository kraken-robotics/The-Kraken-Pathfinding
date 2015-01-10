package tests;

import hook.Hook;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import astar.arc.PathfindingNodes;
import robot.DirectionStrategy;
import robot.Locomotion;
import utils.ConfigInfo;
import utils.Vec2;
import container.ServiceNames;
import exceptions.UnableToMoveException;

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

	@Test
	public void test_suit_chemin() throws Exception
	{
		config.set(ConfigInfo.COULEUR, "jaune");
		locomotion.updateConfig();
		ArrayList<PathfindingNodes> chemin = new ArrayList<PathfindingNodes>();
		chemin.add(PathfindingNodes.BAS);
		chemin.add(PathfindingNodes.DEVANT_DEPART_GAUCHE);
		chemin.add(PathfindingNodes.COTE_MARCHE_GAUCHE);
		chemin.add(PathfindingNodes.DEVANT_DEPART_GAUCHE);
		
		locomotion.followPath(chemin, new ArrayList<Hook>(), DirectionStrategy.FASTEST);
		locomotion.followPath(chemin, new ArrayList<Hook>(), DirectionStrategy.FORCE_FORWARD_MOTION);
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

}
