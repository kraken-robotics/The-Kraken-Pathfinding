package tests;

import org.junit.Before;
import org.junit.Test;

import serie.BufferOutgoingOrder;
import serie.Ticket;
import utils.Sleep;
import container.ServiceNames;

/**
 * Tests unitaires de la série. Plutôt un test des réponses de la STM en fait.
 * @author pf
 *
 */

public class JUnit_Serie extends JUnit_Test {

//	private SerialSTM serie;
	private BufferOutgoingOrder data;
	@Before
    public void setUp() throws Exception {
        super.setUp();
        data = (BufferOutgoingOrder) container.getService(ServiceNames.OUTGOING_ORDER_BUFFER);
//        serie = (SerialSTM) container.getService(ServiceNames.SERIE_STM);
	}
	
	@Test
	public void test_ping() throws Exception
	{
		Sleep.sleep(300000);
	}
	
	@Test
	public void test_ask_color() throws Exception
	{
		Ticket.State etat;
		do {
			Ticket t = data.demandeCouleur();
			synchronized(t)
			{
				if(t.isEmpty())
					t.wait();
			}
			etat = t.getAndClear();
		} while(etat != Ticket.State.OK);
		
		Sleep.sleep(20000);
	}
	
}
