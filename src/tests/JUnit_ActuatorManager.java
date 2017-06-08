package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import robot.cardsWrappers.ActuatorCardWrapper;

public class JUnit_ActuatorManager extends JUnit_Test {

	ActuatorCardWrapper actionneurs;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		log.debug("JUnit_ActionneursTest.setUp()", this);
		actionneurs = (ActuatorCardWrapper)container.getService("Actionneurs");
	}
	
	// TODO : un test par actionneur
	@Test
	public boolean exempleTest() throws Exception
	{

		Assert.assertTrue( 42 != 1337 );
		return true;
	}
}
