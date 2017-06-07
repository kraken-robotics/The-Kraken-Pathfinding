/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

import org.junit.Test;
import kraken.utils.XY_RW;
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
		XY_RW a = new XY_RW(10, 500);
		XY_RW b = new XY_RW(20, -20);
		XY_RW c = new XY_RW();
		Assert.assertTrue(a.equals(a));
		Assert.assertTrue(a.plusNewVector(b).equals(new XY_RW(30, 480)));
		Assert.assertTrue(a.minusNewVector(b).equals(new XY_RW(-10, 520)));
		a.plus(b);
		Assert.assertTrue(a.equals(new XY_RW(30, 480)));
		c.minus(b);
		Assert.assertTrue(c.equals(new XY_RW(-20, 20)));
		Assert.assertTrue(c.squaredDistance(new XY_RW()) == 800);
		Assert.assertTrue(c.dot(a) == (-20 * 30 + 20 * 480));
		c.setX(4);
		c.setY(5);
		Assert.assertTrue(c.distance(new XY_RW(1, 1)) == 5);
		c.setX(3);
		c.setY(4);
		Assert.assertEquals(new XY_RW(0, 1).getX(), new XY_RW(1, 0).rotateNewVector(Math.PI / 2, new XY_RW(0, 0)).getX(), 0.1);
		Assert.assertEquals(new XY_RW(0, 1).getY(), new XY_RW(1, 0).rotateNewVector(Math.PI / 2, new XY_RW(0, 0)).getY(), 0.1);
	}

}
