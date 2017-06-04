/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package kraken.tests;

import org.junit.Test;
import kraken.config.ConfigInfoKraken;

/**
 * Tests unitaires pour la configuration... juste épique.
 * 
 * @author pf
 *
 */

public class JUnit_Config extends JUnit_Test
{

	@Test
	public void test_get() throws Exception
	{
		for(ConfigInfoKraken c : ConfigInfoKraken.values())
			config.getString(c);
	}

}
