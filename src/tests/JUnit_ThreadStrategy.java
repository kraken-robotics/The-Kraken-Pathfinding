package tests;

import org.junit.Before;
import org.junit.Test;

import threads.ThreadStrategy;
import enums.ServiceNames;

/**
 * Tests unitaires de l'arbre des possibles.
 * @author pf
 *
 */

public class JUnit_ThreadStrategy extends JUnit_Test {

	ThreadStrategy threadstrategy;
	
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
		threadstrategy = (ThreadStrategy)container.getService(ServiceNames.THREAD_STRATEGY);
		// le thread n'est pas démarré.
    }

    @Test
    public void test_profondeur_1() throws Exception
    {
    	// un étage maximum. On prévoit uniquement le prochain script
    	threadstrategy.setProfondeurMax(1);
    	threadstrategy.findMeilleureDecision();
    }

}
