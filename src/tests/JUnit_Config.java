package tests;

import org.junit.Test;
import org.junit.Assert;

/**
 * Tests unitaires pour la configuration... juste épique.
 * @author pf
 *
 */

public class JUnit_Config extends JUnit_Test {

	@Test
	public void test_get() throws Exception
	{
		log.debug("JUnit_ReadIniTest.test_get()", this);
		Assert.assertTrue(config.get("test1").equals("test2"));
	}

	@Test
	public void test_set1() throws Exception
	{
		log.debug("JUnit_ReadIniTest.test_set1()", this);
		config.set("test1", "test3");
		Assert.assertTrue(config.get("test1").equals("test3"));
	}
	@Test

	public void test_set2() throws Exception
	{
		log.debug("JUnit_ReadIniTest.test_set2()", this);
		config.set("test1", 3);
		Assert.assertTrue(config.get("test1").equals("3"));
	}

}
