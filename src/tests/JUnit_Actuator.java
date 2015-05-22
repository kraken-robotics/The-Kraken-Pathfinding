package tests;

import org.junit.Before;
import org.junit.Test;

import robot.ActuatorOrder;
import robot.stm.STMcard;
import utils.Sleep;
import container.ServiceNames;

/**
 * Tests unitaires des actionneurs
 * @author pf
 *
 */

public class JUnit_Actuator extends JUnit_Test {

	private STMcard actionneurs;
	
	@Before
    public void setUp() throws Exception {
        super.setUp();
        actionneurs = (STMcard) container.getService(ServiceNames.STM_CARD);
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
