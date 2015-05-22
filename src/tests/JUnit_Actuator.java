package tests;

import org.junit.Before;
import org.junit.Test;

import robot.ActuatorOrder;
import serial.SerialConnexion;
import utils.Sleep;
import container.ServiceNames;

/**
 * Tests unitaires des actionneurs
 * @author pf
 *
 */

public class JUnit_Actuator extends JUnit_Test {

	private SerialConnexion actionneurs;
	
	@Before
    public void setUp() throws Exception {
        super.setUp();
        actionneurs = (SerialConnexion) container.getService(ServiceNames.SERIE_STM);
	}
	
	@Test
	public void test_tous() throws Exception
	{
		for(ActuatorOrder o: ActuatorOrder.values())
		{
			actionneurs.communiquer(o.getSerialOrder());
			Sleep.sleep(o.getSleepValue());
		}
	}
	
}
