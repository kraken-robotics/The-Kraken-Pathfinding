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
		Assert.assertTrue(config.getString(ConfigInfo.MATCH_DEMARRE).equals("true"));
		Assert.assertTrue(config.getBoolean(ConfigInfo.MATCH_DEMARRE));
	}

	@Test
	public void test_set1() throws Exception
	{
		config.set(ConfigInfo.COULEUR, "fushia");
		Assert.assertTrue(config.getString(ConfigInfo.COULEUR).equals("fushia"));
	}
	
	@Test
	public void test_set2() throws Exception
	{
		config.set(ConfigInfo.DATE_DEBUT_MATCH, 42);
		Assert.assertTrue(config.getInt(ConfigInfo.DATE_DEBUT_MATCH) == 42);
	}

}
