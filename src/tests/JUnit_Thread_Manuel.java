package tests;

import org.junit.Before;
import org.junit.Test;

import tests.graphicLib.Fenetre;
import utils.Sleep;

/**
 * Tests unitaires pour tester manuellement l'appel aux différents threads
 * @author pf
 *
 */

public class JUnit_Thread_Manuel extends JUnit_Test {
	
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void test_reveil() throws Exception
    {
//    	Fenetre.getInstance().printObsFixes();
 //   	Fenetre.getInstance().repaint();
    	Sleep.sleep(20000);
    }
	
}
