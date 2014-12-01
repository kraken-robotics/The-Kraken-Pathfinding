package tests;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import enums.ServiceNames;
import robot.cardsWrappers.SensorsCardWrapper;

/**
 * Test des capteurs : les obstacles doivent être détectés
 * TODO : comprendre l'utilité du test desactivation_capteur et faux_test
 * @author marsu
 *
 */
public class JUnit_Sensors extends JUnit_Test
{

	SensorsCardWrapper capteurs;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		log.debug("JUnit_ActionneursTest.setUp()", this);
		capteurs = (SensorsCardWrapper)container.getService(ServiceNames.SENSORS_CARD_WRAPPER);
		config.set("capteurs_on", true);
		capteurs.updateConfig();
	}

	@Test
	public void desactivation_capteur() throws Exception
	{
		log.debug("JUnit_CapteursTest.desactivation_capteur()", this);

		// Avec capteurs
		log.debug(capteurs.mesurer(), this);
	//	Assert.assertTrue(capteurs.mesurer_infrarouge() != 3000);
		Assert.assertTrue(capteurs.mesurer() != 3000);

		// Sans capteurs
		config.set("capteurs_on", false);
<<<<<<< HEAD
		capteurs.maj_config();
=======
		capteurs.updateConfig();
>>>>>>> test4
		log.debug(capteurs.mesurer(), this);
	//	Assert.assertTrue(capteurs.mesurer_infrarouge() == 3000);
		Assert.assertTrue(capteurs.mesurer() == 3000);

		// Et re avec
		config.set("capteurs_on", true);
<<<<<<< HEAD
		capteurs.maj_config();
=======
		capteurs.updateConfig();
>>>>>>> test4
		log.debug(capteurs.mesurer(), this);
	//	Assert.assertTrue(capteurs.mesurer_infrarouge() != 3000);
		Assert.assertTrue(capteurs.mesurer() != 3000);

	}

/*    @Test
    public void faux_test() throws Exception
    {
        config.set("capteurs_on", true);
        for(int i = 0; i < 10000; i++)
        {
            System.out.println(capteurs.mesurer_ultrason());
            Sleep.sleep(100);
        }
    }*/
}
