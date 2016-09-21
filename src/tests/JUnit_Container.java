/*
Copyright (C) 2016 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package tests;

import org.junit.Assert;
import org.junit.Test;

import exceptions.ContainerException;
import obstacles.memory.ObstaclesIteratorPresent;
import obstacles.types.ObstacleProximity;
import pathfinding.astarCourbe.AStarCourbe;
import table.RealTable;
import table.Table;
import tests.container.A;
import tests.container.C;
import tests.container.D;
import utils.Config;
import utils.Vec2RO;

/**
 * Tests unitaires pour le container
 * @author pf
 */

public class JUnit_Container extends JUnit_Test {
	
	@Test
	public void test_instanciation() throws Exception
	{
		container.getService(RealTable.class);
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
	
	@Test
	public void test_extra_param() throws Exception
	{
		container.make(ObstacleProximity.class, new Vec2RO(100, 200), 10, System.currentTimeMillis(), null);
	}


}
