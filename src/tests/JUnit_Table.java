package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import table.Table;
import enums.ServiceNames;

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
    	table.setClapDone(0);
    	int new_hash = table.getHash();
    	Assert.assertNotEquals(old_hash, new_hash);

    	old_hash = new_hash;
    	table.setDistributeurDone(0);
    	new_hash = table.getHash();
    	Assert.assertNotEquals(old_hash, new_hash);

    	old_hash = new_hash;
    	table.setPlotTaken(0);
    	new_hash = table.getHash();
    	Assert.assertNotEquals(old_hash, new_hash);

    	old_hash = new_hash;
    	table.setVerreDone(0);
    	new_hash = table.getHash();
    	Assert.assertNotEquals(old_hash, new_hash);
    }

    @Test
    public void test_clone() throws Exception
    {
    	Table cloned_table = table.clone();
    	Assert.assertTrue(table.equals(cloned_table));
    	cloned_table.setClapDone(0);
    	Assert.assertTrue(!table.equals(cloned_table));
    	cloned_table.copy(table);
    	Assert.assertTrue(table.equals(cloned_table));
    	table.setClapDone(0);
    	Assert.assertTrue(!table.equals(cloned_table));
    }
}
