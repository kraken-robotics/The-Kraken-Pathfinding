package tests;

import org.junit.Test;
import org.junit.Assert;

import utils.Vec2;
import utils.permissions.ReadWrite;

/**
 * Tests unitaires pour Vec2<ReadWrite>
 * @author pf
 *
 */

public class JUnit_Math extends JUnit_Test {

	@Test
	public void test_Vec2() throws Exception
	{
		Vec2<ReadWrite> a = new Vec2<ReadWrite>(10, 500);
		Vec2<ReadWrite> b = new Vec2<ReadWrite>(20, -20);
		Vec2<ReadWrite> c = new Vec2<ReadWrite>();
		Assert.assertTrue(a.equals(a));
		Assert.assertTrue(a.plusNewVector(b).equals(new Vec2<ReadWrite>(30, 480)));
		Assert.assertTrue(a.minusNewVector(b).equals(new Vec2<ReadWrite>(-10, 520)));		
		Vec2.plus(a, b);
		Assert.assertTrue(a.equals(new Vec2<ReadWrite>(30, 480)));
		Vec2.minus(c, b);
		Assert.assertTrue(c.equals(new Vec2<ReadWrite>(-20, 20)));
		Assert.assertTrue(c.squaredDistance(new Vec2<ReadWrite>()) == 800);
		Assert.assertTrue(c.squaredLength() == 800);
		Assert.assertTrue(c.dot(a) == (-20*30+20*480));
		c.x = 4;
		c.y = 5;
		Assert.assertTrue(c.distance(new Vec2<ReadWrite>(1,1)) == 5);
		c.x = 3;
		c.y = 4;
		Assert.assertTrue(c.length() == 5);
		Assert.assertEquals(new Vec2<ReadWrite>(0,1), new Vec2<ReadWrite>(1,0).rotateNewVector(Math.PI/2, new Vec2<ReadWrite>(0,0)));
	}
	
}
