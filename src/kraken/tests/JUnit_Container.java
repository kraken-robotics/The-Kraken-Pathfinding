/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package kraken.tests;

import org.junit.Assert;
import org.junit.Test;
import config.Config;
import injector.InjectorException;
import kraken.obstacles.memory.ObstaclesIteratorPresent;
import kraken.obstacles.types.ObstacleCircular;
import kraken.obstacles.types.ObstacleProximity;
import kraken.pathfinding.astar.AStarCourbe;
import kraken.pathfinding.dstarlite.gridspace.GridSpace;
import kraken.utils.Vec2RO;

/**
 * Tests unitaires pour le container
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
		container.getService(GridSpace.class);
		container.getService(AStarCourbe.class);
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
		container.getService(A.class);
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
		container.getService(C.class);
	}

	/**
	 * Test qui vérifie que la config est bien mise à jour
	 * 
	 * @throws Exception
	 */
	public void test_config() throws Exception
	{
		D d = container.getService(D.class);
		Assert.assertTrue(d.updateConfigOk);
		Assert.assertTrue(d.useConfigOk);
	}

	/**
	 * Test vérifiant que le système de containers se comporte bien si on
	 * appelle deux fois le meme service
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_doublon() throws Exception
	{
		// Config est un service, c'est le même object
		Assert.assertTrue(container.getService(Config.class) == container.getService(Config.class));
	}

}
