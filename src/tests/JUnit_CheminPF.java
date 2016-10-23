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
import org.junit.Before;
import org.junit.Test;

import pathfinding.astarCourbe.arcs.ArcCourbeClotho;
import pathfinding.chemin.CheminPathfinding;
import pathfinding.chemin.IteratorCheminPathfinding;
import robot.RobotReal;

/**
 * Tests unitaires pour le chemin pathfinding
 * @author pf
 *
 */

public class JUnit_CheminPF extends JUnit_Test {

	private CheminPathfinding chemin;
	private IteratorCheminPathfinding iterator;

	@Override
	@Before
    public void setUp() throws Exception {
        super.setUp();
        chemin = container.getService(CheminPathfinding.class);
        iterator = container.make(IteratorCheminPathfinding.class);
    }
	
	@Test
	public void test_iterator() throws Exception
	{
		iterator.reinit();
		Assert.assertEquals(0, iterator.getIndex());
		Assert.assertFalse(iterator.hasNext());
		ArcCourbeClotho arc = new ArcCourbeClotho(container.getService(RobotReal.class));
		chemin.add(arc, true);
		Assert.assertTrue(iterator.hasNext());
	}

}
