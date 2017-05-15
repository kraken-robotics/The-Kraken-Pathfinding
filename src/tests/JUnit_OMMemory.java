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

package tests;

import obstacles.memory.ObstaclesIteratorPresent;
import obstacles.memory.ObstaclesMemory;
import obstacles.types.ObstacleCircular;
import pathfinding.ChronoGameState;
import pathfinding.RealGameState;
import pathfinding.dstarlite.gridspace.GridSpace;
import robot.Cinematique;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import capteurs.SensorsData;
import capteurs.SensorsDataBuffer;
import utils.Vec2RO;

/**
 * Tests unitaires de l'obstacle memory manager
 * 
 * @author pf
 *
 */

public class JUnit_OMMemory extends JUnit_Test
{

	private ObstaclesMemory memory;
	private ObstaclesIteratorPresent iterator;
	private SensorsDataBuffer data;
	// private MasqueManager mm;
	private GridSpace gridspace;
	private RealGameState state;

	@Override
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		state = container.getService(RealGameState.class);
		gridspace = container.getService(GridSpace.class);
		memory = container.getService(ObstaclesMemory.class);
		data = container.getService(SensorsDataBuffer.class);
		iterator = new ObstaclesIteratorPresent(log, memory);
		// mm = container.getService(MasqueManager.class);
	}

	@Test
	public void test_obstacle() throws Exception
	{
		// ChronoGameState chrono = container.make(ChronoGameState.class);
		iterator.reinit();
		Assert.assertTrue(!iterator.hasNext());
		gridspace.addObstacleAndRemoveNearbyObstacles(new ObstacleCircular(new Vec2RO(-400, 1300), 200));
		Assert.assertTrue(iterator.hasNext());
		// state.copyAStarCourbe(chrono);
		// Assert.assertTrue(chrono.iterator.hasNext());
	}

	@Test
	public void test_forme_obstacle() throws Exception
	{
		state.robot.setCinematique(new Cinematique(0, 1500, -Math.PI / 2, true, 0));
		ChronoGameState chrono = container.make(ChronoGameState.class);
		int[] sensors = { 0, 200, 200, 200, 200, 200, 200, 0, 0, 0, 0, 0 };
		data.add(new SensorsData(0, 0, sensors, chrono.robot.getCinematique()));
		Thread.sleep(5000);
	}

	/*
	 * @Test
	 * public void test_iterator() throws Exception
	 * {
	 * Method m = ObstaclesMemory.class.getDeclaredMethod("add", Vec2RO.class,
	 * long.class, Masque.class);
	 * m.setAccessible(true);
	 * int peremption = config.getInt(ConfigInfo.DUREE_PEREMPTION_OBSTACLES);
	 * long date = System.currentTimeMillis();
	 * iterator.reinit();
	 * Assert.assertTrue(memory.getFirstNotDeadNow() == 0);
	 * Assert.assertTrue(memory.getNextDeathDate() == Long.MAX_VALUE);
	 * m.invoke(memory, new Vec2RO(1324,546), date, mm.getMasqueEnnemi(new
	 * Vec2RO(1324,546)));
	 * m.invoke(memory, new Vec2RO(1324,546), date, mm.getMasqueEnnemi(new
	 * Vec2RO(1324,546)));
	 * iterator.reinit();
	 * Assert.assertTrue(iterator.hasNext());
	 * iterator.next();
	 * iterator.remove();
	 * Assert.assertTrue(iterator.hasNext());
	 * iterator.next();
	 * Assert.assertTrue(!iterator.hasNext());
	 * iterator.remove();
	 * Assert.assertTrue(!iterator.hasNext());
	 * m.invoke(memory, new Vec2RO(1324,546), date, mm.getMasqueEnnemi(new
	 * Vec2RO(1324,546)));
	 * Assert.assertEquals(2, memory.getFirstNotDeadNow());
	 * iterator.reinit();
	 * Assert.assertTrue(iterator.hasNext());
	 * ObstacleProximity o = iterator.next();
	 * Assert.assertTrue(o.getPosition().getX() == 1324);
	 * Assert.assertTrue(o.getPosition().getY() == 546);
	 * Assert.assertTrue(!iterator.hasNext());
	 * Assert.assertEquals(2, memory.getFirstNotDeadNow());
	 * m.invoke(memory, new Vec2RO(1324,546), date, mm.getMasqueEnnemi(new
	 * Vec2RO(1324,546)));
	 * m.invoke(memory, new Vec2RO(1324,546), date, mm.getMasqueEnnemi(new
	 * Vec2RO(1324,546)));
	 * m.invoke(memory, new Vec2RO(1324,546), date, mm.getMasqueEnnemi(new
	 * Vec2RO(1324,546)));
	 * m.invoke(memory, new Vec2RO(1324,546), date, mm.getMasqueEnnemi(new
	 * Vec2RO(1324,546)));
	 * memory.deleteOldObstacles();
	 * Assert.assertEquals(2, memory.getFirstNotDeadNow());
	 * Assert.assertEquals(memory.getNextDeathDate(), (date+peremption));
	 * Thread.sleep(peremption+10);
	 * // iterator.reinitNow();
	 * // Assert.assertTrue(memory.getFirstNotDeadNow() == 0);
	 * // Assert.assertTrue(memory.getNextDeathDate() == (date+peremption));
	 * // Assert.assertTrue(iterator.hasNext());
	 * // memory.deleteOldObstacles();
	 * iterator.reinit();
	 * Assert.assertEquals(7, memory.getFirstNotDeadNow());
	 * Assert.assertEquals(7, memory.size());
	 * Assert.assertEquals(Long.MAX_VALUE, memory.getNextDeathDate());
	 * Assert.assertTrue(!iterator.hasNext());
	 * }
	 */
}
