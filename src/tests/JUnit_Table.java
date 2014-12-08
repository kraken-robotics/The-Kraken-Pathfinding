package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import table.Table;
import enums.GameElementNames;
import enums.ServiceNames;
import enums.Tribool;

/**
 * Tests unitaires pour Table
 * @author pf
 *
 */

public class JUnit_Table extends JUnit_Test {

	private Table table;
	
    @Before
    public void setUp() throws Exception {
        super.setUp();
        table = (Table) container.getService(ServiceNames.TABLE);
    }

    @Test
    public void test_modif_hash() throws Exception
    {
    	int old_hash = table.getHash();
    	Assert.assertTrue(table.isDone(GameElementNames.DISTRIB_2) == Tribool.FALSE);
    	table.setDone(GameElementNames.DISTRIB_2);
    	int new_hash = table.getHash();
    	Assert.assertTrue(table.isDone(GameElementNames.DISTRIB_2) == Tribool.TRUE);
    	Assert.assertNotEquals(old_hash, new_hash);
    }

    @Test
    public void test_clone() throws Exception
    {
    	Table cloned_table = table.clone();
    	Assert.assertTrue(table.equals(cloned_table));
        cloned_table.setDone(GameElementNames.CLAP_1);
    	Assert.assertTrue(!table.equals(cloned_table));
    	cloned_table.copy(table);
    	Assert.assertTrue(table.equals(cloned_table));
    	table.setDone(GameElementNames.CLAP_1);
    	Assert.assertTrue(!table.equals(cloned_table));
    }
}
