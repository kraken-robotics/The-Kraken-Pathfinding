package tests;

import org.junit.Assert;
import org.junit.Test;

import container.ServiceNames;

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
			log.debug("JUnit_ContainerTest: "+s.toString());
			Assert.assertTrue(container.getService(s) != null);
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
		for(ServiceNames s : ServiceNames.values())
			Assert.assertTrue(container.getService(s)
					== container.getService(s));
		// comparaison physique entre les deux objets
	}

}
