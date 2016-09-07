package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        table = container.getService(Table.class);
    }

    @Test
    public void test_clone_copy() throws Exception
    {
    	long hash = table.getEtatTable();
    	Table cloned_table = table.clone();
        Assert.assertTrue(hash == cloned_table.getEtatTable());
        cloned_table.setDone(GameElementNames.TRUC, Tribool.TRUE);
        Assert.assertTrue(hash != cloned_table.getEtatTable());
    	cloned_table.copy(table);
    	hash = table.getEtatTable();
        Assert.assertTrue(hash == cloned_table.getEtatTable());
		Assert.assertTrue(table.isDone(GameElementNames.TRUC) == Tribool.TRUE);
		Assert.assertTrue(cloned_table.isDone(GameElementNames.TRUC) == Tribool.TRUE);
    	Assert.assertTrue(table.getEtatTable() == cloned_table.getEtatTable());
    	table.setDone(GameElementNames.MACHIN, Tribool.TRUE);
    	Assert.assertTrue(table.getEtatTable() != cloned_table.getEtatTable());
    }
    
    @Test
    public void test_unicite() throws Exception
    {
    	long hash = table.getEtatTable();
    	for(GameElementNames e : GameElementNames.values)
		{
			table.setDone(e, Tribool.MAYBE);
			Assert.assertTrue(table.getEtatTable() != hash);
			hash = table.getEtatTable();
			table.setDone(e, Tribool.TRUE);
			Assert.assertTrue(table.getEtatTable() != hash);
			hash = table.getEtatTable();
		}
    }
}
