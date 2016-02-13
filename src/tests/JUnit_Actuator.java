package tests;

import org.junit.Before;
import org.junit.Test;

import buffer.DataForSerialOutput;
import robot.ActuatorOrder;
import serie.SerialSTM;
import utils.Sleep;
import container.ServiceNames;

/**
 * Tests unitaires des actionneurs
 * @author pf
 *
 */

public class JUnit_Actuator extends JUnit_Test {

	private SerialSTM actionneurs;
	private DataForSerialOutput data;
	@Before
    public void setUp() throws Exception {
        super.setUp();
        data = (DataForSerialOutput) container.getService(ServiceNames.SERIAL_OUTPUT_BUFFER);
        actionneurs = (SerialSTM) container.getService(ServiceNames.SERIE_STM);
	}
	
	@Test
	public void test_tous() throws Exception
	{
		for(ActuatorOrder o: ActuatorOrder.values())
		{
			// TODO
//			actionneurs.communiquer(String.valueOf(o.ordinal()));
			Sleep.sleep(200);
		}
	}
	
	@Test
	public void test_tous_thread() throws Exception
	{
		for(ActuatorOrder o: ActuatorOrder.values())
		{
			data.utiliseActionneurs(o);
			Sleep.sleep(200);
		}
	}

}
