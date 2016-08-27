package tests;

import org.junit.Before;
import org.junit.Test;

import robot.Speed;
import serie.BufferOutgoingOrder;
import serie.Ticket;
import utils.Sleep;
import container.ServiceNames;

/**
 * Tests unitaires de la série.
 * @author pf
 *
 */

public class JUnit_Serie extends JUnit_Test {

	private BufferOutgoingOrder data;
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
		Sleep.sleep(3000);
	}

	/**
	 * Un test d'ordre long
	 * On arrête dès qu'on a une réponse, qu'elle soit positive ou négative
	 * @throws Exception
	 */
	@Test
	public void test_avancer() throws Exception
	{
		Ticket t = data.avancer(100, Speed.INTO_WALL);
		synchronized(t)
		{
			if(t.isEmpty())
				t.wait();
		}
		log.debug("Avancer fini, code : "+t.getAndClear());
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
		
		Sleep.sleep(20000);
	}
	
}
