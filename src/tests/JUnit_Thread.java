package tests;

import org.junit.Before;
import org.junit.Test;

import threads.ThreadStrategy;
import utils.Config;
import utils.Sleep;
import enums.ServiceNames;

/**
 * Tests unitaires des threads.
 * @author pf
 *
 */

public class JUnit_Thread extends JUnit_Test {

	ThreadStrategy threadstrategy;
	
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
		threadstrategy = (ThreadStrategy)container.getService(ServiceNames.THREAD_STRATEGY);
		// le thread n'est pas démarré.
    }
    
    @Test
    public void test_threads() throws Exception
    {
    	container.startAllThreads();
    	Config.matchDemarre = true;
    	// on laisse tourner un peu pour vérifier qu'il n'y a pas d'erreurs
    	Sleep.sleep(5000);
    	container.stopAllThreads();
    }

}
