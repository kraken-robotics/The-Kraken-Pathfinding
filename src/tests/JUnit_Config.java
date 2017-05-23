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

import org.junit.Test;
import org.junit.Assert;
import config.ConfigInfo;
import robot.RobotColor;

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
		for(ConfigInfo c : ConfigInfo.values())
			config.getString(c);
	}

	@Test
	public void test_set1() throws Exception
	{
		Assert.assertTrue(config.getSymmetry() == null);
		config.set(ConfigInfo.COULEUR, RobotColor.BLEU);
		Assert.assertTrue(config.getSymmetry() == true);
	}

	@Test
	public void test_set2() throws Exception
	{
		config.set(ConfigInfo.DATE_DEBUT_MATCH, 42);
		Assert.assertTrue(config.getInt(ConfigInfo.DATE_DEBUT_MATCH) == 42);
	}

}
