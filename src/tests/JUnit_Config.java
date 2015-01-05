package tests;

import org.junit.Test;
import org.junit.Assert;

import utils.ConfigInfo;

/**
 * Tests unitaires pour la configuration... juste épique.
 * @author pf
 *
 */

public class JUnit_Config extends JUnit_Test {

	@Test
	public void test_get() throws Exception
	{
		for(ConfigInfo c: ConfigInfo.values())
			config.getString(c);
		Assert.assertTrue(config.getString(ConfigInfo.TEST1).equals("test2"));
	}

	@Test
	public void test_set1() throws Exception
	{
		log.debug("JUnit_ReadIniTest.test_set1()", this);
		config.set(ConfigInfo.TEST1, "test3");
		Assert.assertTrue(config.getString(ConfigInfo.TEST1).equals("test3"));
	}
	
	@Test
	public void test_set2() throws Exception
	{
		log.debug("JUnit_ReadIniTest.test_set2()", this);
		config.set(ConfigInfo.TEST1, 3);
		Assert.assertTrue(config.getString(ConfigInfo.TEST1).equals("3"));
	}

}
