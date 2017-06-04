/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package kraken.tests;

import org.junit.Test;
import kraken.utils.Vec2RW;
import org.junit.Assert;

/**
 * Tests unitaires pour Vec2RW
 * 
 * @author pf
 *
 */

public class JUnit_Math extends JUnit_Test
{

	@Test
	public void test_Vec2() throws Exception
	{
		Vec2RW a = new Vec2RW(10, 500);
		Vec2RW b = new Vec2RW(20, -20);
		Vec2RW c = new Vec2RW();
		Assert.assertTrue(a.equals(a));
		Assert.assertTrue(a.plusNewVector(b).equals(new Vec2RW(30, 480)));
		Assert.assertTrue(a.minusNewVector(b).equals(new Vec2RW(-10, 520)));
		a.plus(b);
		Assert.assertTrue(a.equals(new Vec2RW(30, 480)));
		c.minus(b);
		Assert.assertTrue(c.equals(new Vec2RW(-20, 20)));
		Assert.assertTrue(c.squaredDistance(new Vec2RW()) == 800);
		Assert.assertTrue(c.dot(a) == (-20 * 30 + 20 * 480));
		c.setX(4);
		c.setY(5);
		Assert.assertTrue(c.distance(new Vec2RW(1, 1)) == 5);
		c.setX(3);
		c.setY(4);
		Assert.assertEquals(new Vec2RW(0, 1).getX(), new Vec2RW(1, 0).rotateNewVector(Math.PI / 2, new Vec2RW(0, 0)).getX(), 0.1);
		Assert.assertEquals(new Vec2RW(0, 1).getY(), new Vec2RW(1, 0).rotateNewVector(Math.PI / 2, new Vec2RW(0, 0)).getY(), 0.1);
	}

}
