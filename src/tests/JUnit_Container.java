package tests;

import org.junit.Assert;
import org.junit.Test;

import exceptions.ContainerException;
import pathfinding.astarCourbe.AStarCourbe;
import table.Table;
import tests.circular.A;
import utils.Config;

/**
 * Tests unitaires pour le container
 * Sert surtout à vérifier l'absence de dépendances circulaires.
 * @author pf
 */

public class JUnit_Container extends JUnit_Test {
	
	@Test
	public void test_instanciation() throws Exception
	{
		container.getService(Table.class);
		container.getService(AStarCourbe.class);
	}

	@Test(expected = ContainerException.class)
	public void test_dependance_circulaire() throws Exception
	{
		container.getService(A.class);
	}

	/**
	 * Test vérifiant que le système de containers se comporte bien si on appelle deux fois le meme service 
	 * @throws Exception
	 */
	@Test
	public void test_doublon() throws Exception
	{
		Assert.assertTrue(container.getService(Config.class)
				== container.getService(Config.class));
		// comparaison physique entre les deux objets
	}

}
