package tests;

import org.junit.Before;
import org.junit.Test;

import robot.actuator.ActuatorOrder;
import serie.BufferOutgoingOrder;
import serie.SerieCouchePhysique;
import utils.Sleep;
import container.ServiceNames;

/**
 * Tests unitaires des actionneurs
 * @author pf
 *
 */

public class JUnit_Actuator extends JUnit_Test {

	private SerieCouchePhysique actionneurs;
	private BufferOutgoingOrder data;
	@Before
    public void setUp() throws Exception {
        super.setUp();
        data = (BufferOutgoingOrder) container.getService(ServiceNames.SERIAL_OUTPUT_BUFFER);
        actionneurs = (SerieCouchePhysique) container.getService(ServiceNames.SERIE);
	}
	

	@Test
	public void test_angle() throws Exception
	{
		data.utiliseActionneurs(ActuatorOrder.TEST);
/*		for(int i = 200; i < 900; i+= 10)
		{
			log.debug(i);
			data.utiliseActionneurs(3, i);
			Sleep.sleep(500);
		}*/
	}
	
	@Test
	public void test_tous() throws Exception
	{
		for(ActuatorOrder o: ActuatorOrder.values())
		{
			log.debug(o);
			data.utiliseActionneurs(o);
			Sleep.sleep(2000);
		}
	}

}
