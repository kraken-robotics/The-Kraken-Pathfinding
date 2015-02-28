package tests;

import org.junit.Before;
import org.junit.Test;

import robot.cardsWrappers.ActuatorCardWrapper;
import robot.cardsWrappers.enums.ActuatorOrder;
import utils.Sleep;
import container.ServiceNames;

/**
 * Tests unitaires des actionneurs
 * @author pf
 *
 */

public class JUnit_Actuator extends JUnit_Test {

	ActuatorCardWrapper actionneurs;
	
	@Before
    public void setUp() throws Exception {
        super.setUp();
        actionneurs = (ActuatorCardWrapper) container.getService(ServiceNames.ACTUATOR_CARD_WRAPPER);
	}
	
	@Test
	public void test_tous() throws Exception
	{
		for(ActuatorOrder o: ActuatorOrder.values())
		{
			actionneurs.useActuator(o);
			if(o.hasSymmetry())
				actionneurs.useActuator(o.getSymmetry());
			Sleep.sleep(o.getSleepValue());
		}
	}
	
}
