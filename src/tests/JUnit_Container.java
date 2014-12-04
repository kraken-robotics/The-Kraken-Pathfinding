package tests;

import org.junit.Assert;
import org.junit.Test;

import container.Service;
import enums.ServiceNames;

/**
 * Tests unitaires pour le container
 * Sert surtout à vérifier l'absence de dépendances circulaires.
 * @author pf
 */

public class JUnit_Container extends JUnit_Test {
	
	@Test
	public void test_instanciation() throws Exception
	{
		for(ServiceNames s : ServiceNames.values())
		{
			log.debug("JUnit_ContainerTest: "+s.toString(), this);
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
		Service s1 = container.getService(ServiceNames.LOCOMOTION);
		Service s2 = container.getService(ServiceNames.LOCOMOTION);
		Assert.assertTrue(s1 == s2); // comparaison physique entre les deux objets
	}

}
