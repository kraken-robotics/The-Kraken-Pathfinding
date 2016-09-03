package tests.lowlevel;

import org.junit.Before;
import org.junit.Test;

import serie.BufferOutgoingOrder;
import serie.Ticket;
import tests.JUnit_Test;
import container.ServiceNames;

/**
 * Tests unitaires de la série.
 * @author pf
 *
 */

public class JUnit_Serie extends JUnit_Test {

	private BufferOutgoingOrder data;
	@Override
	@Before
    public void setUp() throws Exception {
        super.setUp();
        data = (BufferOutgoingOrder) container.getService(ServiceNames.OUTGOING_ORDER_BUFFER);
	}
	
	/**
	 * Un test pour vérifie la connexion
	 * Le programme s'arrête automatiquement au bout de 3s
	 * @throws Exception
	 */
	@Test
	public void test_ping() throws Exception
	{
		Thread.sleep(3000);
	}

	/**
	 * Un test d'ordre long
	 * @throws Exception
	 */
	@Test
	public void test_stream() throws Exception
	{
		data.startStream();
		Thread.sleep(10000);
	}

	/**
	 * Un test d'ordre court.
	 * On redemande la couleur jusqu'à avoir un autre code que "couleur inconnue" 
	 * @throws Exception
	 */
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
	}
	
}
