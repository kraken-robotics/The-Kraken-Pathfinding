package tests;

import org.junit.Assert;
import org.junit.Test;

import exceptions.ContainerException;
import obstacles.memory.ObstaclesIteratorPresent;
import pathfinding.astarCourbe.AStarCourbe;
import table.Table;
import tests.container.A;
import tests.container.C;
import tests.container.D;
import utils.Config;

/**
 * Tests unitaires pour le container
 * @author pf
 */

public class JUnit_Container extends JUnit_Test {
	
	@Test
	public void test_instanciation() throws Exception
	{
		container.getService(Table.class);
		container.getService(AStarCourbe.class);
	}

	/**
	 * Test qui vérifie qu'une exception est bien levée en cas de dépendance circulaire
	 * @throws Exception
	 */
	@Test(expected = ContainerException.class)
	public void test_dependance_circulaire() throws Exception
	{
		container.getService(A.class);
	}

	/**
	 * Test qui vérifie si une exception est bien levée si un Service a plusieurs constructeurs
	 * @throws Exception
	 */
	@Test(expected = ContainerException.class)
	public void test_multi_constructeur() throws Exception
	{
		container.getService(C.class);
	}

	/**
	 * Test qui vérifie que la config est bien mise à jour
	 * @throws Exception
	 */
	public void test_config() throws Exception
	{
		D d = container.getService(D.class);
		Assert.assertTrue(d.updateConfigOk);
		Assert.assertTrue(d.useConfigOk);
	}

	/**
	 * Test vérifiant que le système de containers se comporte bien si on appelle deux fois le meme service 
	 * @throws Exception
	 */
	@Test
	public void test_doublon() throws Exception
	{
		// Config est un service, c'est le même object
		Assert.assertTrue(container.getService(Config.class)
				== container.getService(Config.class));
		// ObstaclesIteratorPresent n'est pas un service : deux objets différents
		Assert.assertTrue(container.make(ObstaclesIteratorPresent.class)
				!= container.make(ObstaclesIteratorPresent.class));	
	}

}
