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

import java.lang.reflect.Field;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import table.GameElementNames;
import table.RealTable;
import table.Table;
import table.EtatElement;

/**
 * Tests unitaires pour Table
 * 
 * @author pf
 *
 */

public class JUnit_Table extends JUnit_Test
{

	private Table table;

	@Override
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		table = container.getService(RealTable.class);
	}

	@Test
	public void test_clone_copy() throws Exception
	{
		Field f = Table.class.getDeclaredField("etatTable");
		f.setAccessible(true);

		long hash = f.getLong(table);
		Table cloned_table = table.clone();
		Assert.assertTrue(hash == f.getLong(cloned_table));
		cloned_table.setDone(GameElementNames.CYLINDRE_2_D, EtatElement.PRIS_PAR_NOUS);
		Assert.assertTrue(hash != f.getLong(cloned_table));
		cloned_table.copy(table);
		hash = f.getLong(table);
		Assert.assertTrue(hash == f.getLong(cloned_table));
		Assert.assertTrue(table.isDone(GameElementNames.CYLINDRE_2_D) == EtatElement.PRIS_PAR_NOUS);
		Assert.assertTrue(cloned_table.isDone(GameElementNames.CYLINDRE_2_D) == EtatElement.PRIS_PAR_NOUS);
		Assert.assertTrue(f.getLong(table) == f.getLong(cloned_table));
		table.setDone(GameElementNames.CYLINDRE_3_D, EtatElement.PRIS_PAR_NOUS);
		Assert.assertTrue(f.getLong(table) != f.getLong(cloned_table));
	}

	@Test
	public void test_unicite() throws Exception
	{
		Field f = Table.class.getDeclaredField("etatTable");
		f.setAccessible(true);

		long hash = f.getLong(table);
		for(GameElementNames e : GameElementNames.values())
		{
			table.setDone(e, EtatElement.PRIS_PAR_ENNEMI);
			Assert.assertTrue(f.getLong(table) != hash);
			hash = f.getLong(table);
			table.setDone(e, EtatElement.PRIS_PAR_NOUS);
			Assert.assertTrue(f.getLong(table) != hash);
			hash = f.getLong(table);
		}
	}
}
