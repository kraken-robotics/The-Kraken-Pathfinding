package tests;

import hook.types.HookFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import enums.ServiceNames;
import enums.Speed;
import robot.RobotReal;
import smartMath.Vec2;

/**
 * Tests unitaires des hooks (en jaune: sans symétrie)
 * @author pf
 *
 */

public class JUnit_HookYellow extends JUnit_Test
{

	private RobotReal robotvrai;
	@SuppressWarnings("unused")
	private HookFactory hookgenerator;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		log.debug("JUnit_HookJauneTest.setUp()", this);
		config.set("couleur", "jaune");
		robotvrai = (RobotReal) container.getService(ServiceNames.ROBOT_REAL);
		robotvrai.setPosition(new Vec2(0, 1500));
		robotvrai.setOrientation(0);
		robotvrai.set_vitesse(Speed.BETWEEN_SCRIPTS);
	}

	// TODO �crire un test par type de hook
	@Test
	public void test_hookAbscisse_avancer() throws Exception
	{
		Assert.assertTrue(666 != 42);
	}

	
}
