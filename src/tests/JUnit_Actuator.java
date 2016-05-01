package tests;

import org.junit.Before;
import org.junit.Test;

import robot.actuator.ActuatorOrder;
import serie.DataForSerialOutput;
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
	public void test_angle() throws Exception
	{
		data.utiliseActionneurs(ActuatorOrder.AX12_ARRIERE_GAUCHE_VERR1);
		Sleep.sleep(2000);
		data.utiliseActionneurs(ActuatorOrder.AX12_AVANT_GAUCHE_OUVERT1);
		Sleep.sleep(2000);
		data.utiliseActionneurs(ActuatorOrder.AX12_AVANT_GAUCHE_FERME);
		data.utiliseActionneurs(ActuatorOrder.AX12_ARRIERE_GAUCHE_FERME);
		Sleep.sleep(2000);
		data.utiliseActionneurs(ActuatorOrder.AX12_ARRIERE_GAUCHE_VERR2);
		Sleep.sleep(2000);
		data.utiliseActionneurs(ActuatorOrder.AX12_AVANT_GAUCHE_OUVERT2);
		Sleep.sleep(2000);
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
			// TODO test AX12
			log.debug(o);
			data.utiliseActionneurs(o);
//			actionneurs.communiquer(String.valueOf(o.ordinal()));
			Sleep.sleep(1000);
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
