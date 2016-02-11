package tests;

import org.junit.Before;
import org.junit.Test;

import buffer.DataForSerialOutput;
import utils.SerialSTM;
import container.ServiceNames;

/**
 * Tests unitaires de la série. Plutôt un test des réponses de la STM en fait.
 * @author pf
 *
 */

public class JUnit_Serie extends JUnit_Test {

	private SerialSTM serie;
	private DataForSerialOutput data;
	@Before
    public void setUp() throws Exception {
        super.setUp();
        data = (DataForSerialOutput) container.getService(ServiceNames.SERIAL_OUTPUT_BUFFER);
        serie = (SerialSTM) container.getService(ServiceNames.SERIE_STM);
	}
	
	@Test
	public void test_ping() throws Exception
	{}
	
}
