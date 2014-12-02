package tests;

import org.junit.Assert;
import org.junit.Test;

import enums.ServiceNames;

/**
 * Tests unitaires pour le container
 * Sert surtout à vérifier l'absence de dépendances circulaires, et d'éventuelles fautes de frappe...
 * @author pf
 */
public class JUnit_Container extends JUnit_Test {
	
	@Test
	public void test_instanciation() throws Exception
	{
		for(ServiceNames s : ServiceNames.values())
		{
			log.debug("JUnit_ContainerTest.test_log()", this);
			container.getService(s);
		}
		Assert.assertTrue(container.afficheNonInstancies());
	}

	/**
	 * Test vérifiant que le système de containers se comporte bien si on appelle deux fois  le meme service 
	 * @throws Exception
	 */
	@Test
	public void test_doublon() throws Exception
	{
		log.debug("JUnit_ContainerTest.test_doublon()", this);
		container.getService(ServiceNames.LOCOMOTION);
		container.getService(ServiceNames.LOCOMOTION);
	}

}
