package tests;

import org.junit.Test;
import org.junit.Assert;

import exceptions.MatrixException;
import smartMath.Matrn;
import smartMath.Vec2;

/**
 * Tests unitaires pour le package smartMath
 * @author pf
 *
 */

public class JUnit_Math extends JUnit_Test {

	Matrn y;
	Matrn z;
	
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

	@Test public void test_matrn_constructor() throws Exception
	{
		log.debug("JUnit_MathTest.test_matrn_constructor()", this);
		y = new Matrn(2);
		Assert.assertTrue(y.getNbColonnes() == 2);
		Assert.assertTrue(y.getNbLignes() == 2);
		y = new Matrn(2, 1);
		Assert.assertTrue(y.getNbColonnes() == 1);
		Assert.assertTrue(y.getNbLignes() == 2);
		double[][] tab = new double[2][1];
		tab[0][0] = 1;
		tab[1][0] = 2;
		y = new Matrn(tab);
		Assert.assertTrue(y.getNbColonnes() == 2);
		Assert.assertTrue(y.getNbLignes() == 1);
	}

	@Test
	public void test_add() throws Exception
	{
		log.debug("JUnit_MathTest.test_add()", this);
		y = new Matrn(2);
		y.setCoeff(1, 0, 0);
		y.setCoeff(2, 0, 1);
		y.setCoeff(3, 1, 0);
		y.setCoeff(4, 1, 1);

		z = new Matrn(2);
		z.setCoeff(5, 0, 0);
		z.setCoeff(8, 0, 1);
		z.setCoeff(2, 1, 0);
		z.setCoeff(12, 1, 1);

		y.additionner_egal(z);
		Assert.assertTrue(y.getCoeff(0, 0) == 6);
		Assert.assertTrue(y.getCoeff(0, 1) == 10);
		Assert.assertTrue(y.getCoeff(1, 0) == 5);
		Assert.assertTrue(y.getCoeff(1, 1) == 16);
	}

	@Test
	public void test_add_2() throws Exception
	{
		log.debug("JUnit_MathTest.test_add_2()", this);
		y = new Matrn(2);
		y.setCoeff(1, 0, 0);
		y.setCoeff(2, 0, 1);
		y.setCoeff(3, 1, 0);
		y.setCoeff(4, 1, 1);

		z = new Matrn(2);
		z.setCoeff(5, 0, 0);
		z.setCoeff(8, 0, 1);
		z.setCoeff(2, 1, 0);
		z.setCoeff(12, 1, 1);

		Matrn a = y.additionner(z);
		Assert.assertTrue(a.getCoeff(0, 0) == 6);
		Assert.assertTrue(a.getCoeff(0, 1) == 10);
		Assert.assertTrue(a.getCoeff(1, 0) == 5);
		Assert.assertTrue(a.getCoeff(1, 1) == 16);
	}

	@Test
	public void test_mul() throws Exception
	{
		log.debug("JUnit_MathTest.test_mul()", this);
		y = new Matrn(2);
		y.setCoeff(1, 0, 0);
		y.setCoeff(2, 0, 1);
		y.setCoeff(3, 1, 0);
		y.setCoeff(4, 1, 1);

		z = new Matrn(2);
		z.setCoeff(5, 0, 0);
		z.setCoeff(8, 0, 1);
		z.setCoeff(2, 1, 0);
		z.setCoeff(12, 1, 1);

		y.multiplier_egal(z);
		Assert.assertTrue(y.getCoeff(0, 0) == 9);
		Assert.assertTrue(y.getCoeff(0, 1) == 32);
		Assert.assertTrue(y.getCoeff(1, 0) == 23);
		Assert.assertTrue(y.getCoeff(1, 1) == 72);
	}

	@Test
	public void test_mul_2() throws Exception
	{
		log.debug("JUnit_MathTest.test_mul_2()", this);
		y = new Matrn(2);
		y.setCoeff(1, 0, 0);
		y.setCoeff(2, 0, 1);
		y.setCoeff(3, 1, 0);
		y.setCoeff(4, 1, 1);

		z = new Matrn(2);
		z.setCoeff(5, 0, 0);
		z.setCoeff(8, 0, 1);
		z.setCoeff(2, 1, 0);
		z.setCoeff(12, 1, 1);

		Matrn a = y.multiplier(z);
		Assert.assertTrue(a.getCoeff(0, 0) == 9);
		Assert.assertTrue(a.getCoeff(0, 1) == 32);
		Assert.assertTrue(a.getCoeff(1, 0) == 23);
		Assert.assertTrue(a.getCoeff(1, 1) == 72);
	}

	@Test
	public void test_transpose() throws Exception
	{
		log.debug("JUnit_MathTest.test_transpose()", this);
		y = new Matrn(3);
		y.setCoeff(1, 0, 0);
		y.setCoeff(2, 0, 1);
		y.setCoeff(12, 0, 2);		
		y.setCoeff(3, 1, 0);
		y.setCoeff(4, 1, 1);
		y.setCoeff(-1, 1, 2);
		y.setCoeff(51, 2, 0);
		y.setCoeff(-12, 2, 1);
		y.setCoeff(0, 2, 2);

		y.transpose_egal();
		Assert.assertTrue(y.getCoeff(0, 0) == 1);
		Assert.assertTrue(y.getCoeff(0, 1) == 3);
		Assert.assertTrue(y.getCoeff(0, 2) == 51);
		Assert.assertTrue(y.getCoeff(1, 0) == 2);
		Assert.assertTrue(y.getCoeff(1, 1) == 4);
		Assert.assertTrue(y.getCoeff(1, 2) == -12);
		Assert.assertTrue(y.getCoeff(2, 0) == 12);
		Assert.assertTrue(y.getCoeff(2, 1) == -1);
		Assert.assertTrue(y.getCoeff(2, 2) == 0);
	}

	@Test
	public void test_transpose_2() throws Exception
	{
		log.debug("JUnit_MathTest.test_transpose_2()", this);
		y = new Matrn(3);
		y.setCoeff(1, 0, 0);
		y.setCoeff(2, 0, 1);
		y.setCoeff(12, 0, 2);		
		y.setCoeff(3, 1, 0);
		y.setCoeff(4, 1, 1);
		y.setCoeff(-1, 1, 2);
		y.setCoeff(51, 2, 0);
		y.setCoeff(-12, 2, 1);
		y.setCoeff(0, 2, 2);
		
		Matrn a = y.transpose();
		Assert.assertTrue(a.getCoeff(0, 0) == 1);
		Assert.assertTrue(a.getCoeff(0, 1) == 3);
		Assert.assertTrue(a.getCoeff(0, 2) == 51);
		Assert.assertTrue(a.getCoeff(1, 0) == 2);
		Assert.assertTrue(a.getCoeff(1, 1) == 4);
		Assert.assertTrue(a.getCoeff(1, 2) == -12);
		Assert.assertTrue(a.getCoeff(2, 0) == 12);
		Assert.assertTrue(a.getCoeff(2, 1) == -1);
		Assert.assertTrue(a.getCoeff(2, 2) == 0);
	}

	@Test(expected=MatrixException.class)
	public void test_exception_add() throws Exception
	{
		log.debug("JUnit_MathTest.test_exception_add()", this);
		y = new Matrn(2);
		y.setCoeff(1, 0, 0);
		y.setCoeff(2, 0, 1);
		y.setCoeff(3, 1, 0);
		y.setCoeff(4, 1, 1);

		z = new Matrn(1);
		z.setCoeff(5, 0, 0);

		y.additionner_egal(z);
	}

	@Test(expected=MatrixException.class)
	public void test_exception_mul() throws Exception
	{
		log.debug("JUnit_MathTest.test_exception_mul()", this);
		y = new Matrn(1,2);
		y.setCoeff(1, 0, 0);
		y.setCoeff(2, 0, 1);

		z = new Matrn(1,1);
		z.setCoeff(5, 0, 0);

		y.multiplier_egal(z);
	}

	@Test(expected=MatrixException.class)
	public void test_exception_transpose() throws Exception
	{
		log.debug("JUnit_MathTest.test_exception_transpose()", this);
		y = new Matrn(1,2);
		y.setCoeff(1, 0, 0);
		y.setCoeff(2, 0, 1);

		y.transpose_egal();
	}
	
}
