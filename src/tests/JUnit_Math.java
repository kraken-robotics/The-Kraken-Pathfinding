package tests;

import org.junit.Test;
import org.junit.Assert;

import smartMath.Vec2;

/**
 * Tests unitaires pour le package smartMath
 * @author pf
 *
 */

public class JUnit_Math extends JUnit_Test {

	@Test
	public void test_Vec2() throws Exception
	{
		log.debug("JUnit_MathTest.test_Vec2()", this);
		Vec2 a = new Vec2(10, 500);
		Vec2 b = new Vec2(20, -20);
		Vec2 c = new Vec2();
		Assert.assertTrue(a.equals(a));
		Assert.assertTrue(a.plusNewVector(b).equals(new Vec2(30, 480)));
		Assert.assertTrue(a.minusNewVector(b).equals(new Vec2(-10, 520)));		
		a.plus(b);
		Assert.assertTrue(a.equals(new Vec2(30, 480)));
		c.minus(b);
		Assert.assertTrue(c.equals(new Vec2(-20, 20)));
		Assert.assertTrue(c.squaredDistance(new Vec2()) == 800);
		Assert.assertTrue(c.squaredLength() == 800);
		Assert.assertTrue(c.dot(a) == (-20*30+20*480));
		c.x = 4;
		c.y = 5;
		Assert.assertTrue(c.distance(new Vec2(1,1)) == 5);
		c.x = 3;
		c.y = 4;
		Assert.assertTrue(c.length() == 5);
	}
	
}
