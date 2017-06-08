package tests;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import robot.*;
import smartMath.Vec2;
/**
 * Tests unitaires pour RobotChrono
 * @author pf
 *
 */
public class JUnit_RobotChrono extends JUnit_Test {

	private RobotChrono robotchrono;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		log.debug("JUnit_RobotChronoTest.setUp()", this);
		robotchrono = new RobotChrono(config, log);
		robotchrono.setPosition(new Vec2(0, 1500));
		robotchrono.setOrientation(0);
	}

	@Test
	public void test_avancer() throws Exception
	{
		log.debug("JUnit_RobotChronoTest.test_avancer()", this);
		robotchrono.avancer(10);
		System.out.println("Avant: "+robotchrono.getPosition());
		Assert.assertTrue(robotchrono.getPosition().equals(new Vec2(10,1500)));
        System.out.println("Après: "+robotchrono.getPosition());
	}

	@Test
	public void test_va_au_point_symetrie() throws Exception
	{
		log.debug("JUnit_RobotChronoTest.test_va_au_point_symetrie()", this);
		config.set("couleur", "jaune");
		robotchrono.updateConfig();
		robotchrono = new RobotChrono(config, log);
		robotchrono.setPosition(new Vec2(0, 1500));
		robotchrono.setOrientation(0);
		robotchrono.va_au_point(new Vec2(10, 1400));
		Assert.assertTrue(robotchrono.getPosition().distance(new Vec2(10,1400)) < 2);

		config.set("couleur", "rouge");
		robotchrono = new RobotChrono(config, log);
		robotchrono.setPosition(new Vec2(0, 1500));
		robotchrono.setOrientation(0);
		robotchrono.va_au_point(new Vec2(10, 1400));
		Assert.assertTrue(robotchrono.getPosition().distance(new Vec2(-10,1400)) < 2);
	}
	
	@Test
	public void test_va_au_point() throws Exception
	{
		log.debug("JUnit_RobotChronoTest.test_va_au_point()", this);
		robotchrono.va_au_point(new Vec2(10, 1400));
		Assert.assertTrue(robotchrono.getPosition().distance(new Vec2(10,1400)) < 2);
	}

	@Test
	public void test_tourner() throws Exception
	{
		log.debug("JUnit_RobotChronoTest.test_tourner()", this);
		robotchrono.tourner((float)1.2);
		Assert.assertTrue(robotchrono.getOrientation()==(float)1.2);
	}

	@Test
	public void test_suit_chemin() throws Exception
	{
		log.debug("JUnit_RobotChronoTest.test_suit_chemin()", this);
		ArrayList<Vec2> chemin = new ArrayList<Vec2>();
		chemin.add(new Vec2(20, 1400));
		chemin.add(new Vec2(40, 1500));
		robotchrono.suit_chemin(chemin, null);
		Assert.assertTrue(robotchrono.getPosition().distance(new Vec2(40,1500)) < 2);
		
	}
	
	@Test
	public void test_actionneurs() throws Exception
	{
		// TODO: tester les actionneurs de robotchrono
	}
		
}
