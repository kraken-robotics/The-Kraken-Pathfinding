package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import container.ServiceNames;
import table.GameElementNames;
import table.Table;
import enums.Tribool;

/**
 * Tests unitaires pour Table
 * @author pf
 *
 */

public class JUnit_Table extends JUnit_Test {

	private Table table;
	
    @Override
	@Before
    public void setUp() throws Exception {
        super.setUp();
        table = (Table) container.getService(ServiceNames.TABLE);
    }

    @Test
    public void test_clone_copy() throws Exception
    {
    	int hash = table.getHashLPAStar();
    	Table cloned_table = table.clone();
    	Assert.assertTrue(table.equals(cloned_table));
        Assert.assertTrue(hash == cloned_table.getHashLPAStar());
        cloned_table.setDone(GameElementNames.TRUC, Tribool.TRUE);
        Assert.assertTrue(hash != cloned_table.getHashLPAStar());
    	Assert.assertTrue(!table.equals(cloned_table));
    	cloned_table.copy(table);
    	hash = table.getHashLPAStar();
        Assert.assertTrue(hash == cloned_table.getHashLPAStar());
		Assert.assertTrue(table.isDone(GameElementNames.TRUC) == Tribool.TRUE);
		Assert.assertTrue(cloned_table.isDone(GameElementNames.TRUC) == Tribool.TRUE);
    	Assert.assertTrue(table.equals(cloned_table));
    	table.setDone(GameElementNames.MACHIN, Tribool.TRUE);
    	Assert.assertTrue(!table.equals(cloned_table));
    }
    
    @Test
    public void test_unicite() throws Exception
    {
    	long hash = table.getEtatTable();
    	int code = table.hashCode();
    	for(GameElementNames e : GameElementNames.values)
		{
			table.setDone(e, Tribool.MAYBE);
			Assert.assertTrue(table.getEtatTable() != hash);
			hash = table.getEtatTable();
			table.setDone(e, Tribool.TRUE);
			Assert.assertTrue(table.getEtatTable() != hash);
			Assert.assertTrue(table.hashCode() != code);
			hash = table.getEtatTable();
			code = table.hashCode();
		}
    }
}
