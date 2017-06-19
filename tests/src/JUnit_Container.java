/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

import org.junit.Assert;
import org.junit.Test;
import config.Config;
import injector.InjectorException;
import pfg.kraken.astar.TentacularAStar;
import pfg.kraken.dstarlite.Navmesh;
import pfg.kraken.utils.XY;

/**
 * Tests unitaires pour l'injector
 * 
 * @author pf
 */

public class JUnit_Container extends JUnit_Test
{

	public class A
	{
		public A(B b)
		{}
	}

	public class B
	{
		public B(A a)
		{}
	}

	public class C
	{
		public C(B b)
		{}

		public C(A a)
		{}
	}

	public class D
	{
		public boolean useConfigOk = false;
		public boolean updateConfigOk = false;

		public D()
		{}
	}

	@Test
	public void test_instanciation() throws Exception
	{
		injector.getService(Navmesh.class);
		injector.getService(TentacularAStar.class);
	}

	/**
	 * Test qui vérifie qu'une exception est bien levée en cas de dépendance
	 * circulaire
	 * 
	 * @throws Exception
	 */
	@Test(expected = InjectorException.class)
	public void test_dependance_circulaire() throws Exception
	{
		injector.getService(A.class);
	}

	/**
	 * Test qui vérifie si une exception est bien levée si un Service a
	 * plusieurs constructeurs
	 * 
	 * @throws Exception
	 */
	@Test(expected = InjectorException.class)
	public void test_multi_constructeur() throws Exception
	{
		injector.getService(C.class);
	}

	/**
	 * Test qui vérifie que la config est bien mise à jour
	 * 
	 * @throws Exception
	 */
	public void test_config() throws Exception
	{
		D d = injector.getService(D.class);
		Assert.assertTrue(d.updateConfigOk);
		Assert.assertTrue(d.useConfigOk);
	}

	/**
	 * Test vérifiant que le système de injectors se comporte bien si on
	 * appelle deux fois le meme service
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_doublon() throws Exception
	{
		// Config est un service, c'est le même object
		Assert.assertTrue(injector.getService(Config.class) == injector.getService(Config.class));
	}
}
