package tests;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import obstacles.types.Obstacle;
import table.GameElementNames;
import table.Table;
import table.Tribool;
import utils.Vec2;
import utils.permissions.ReadWrite;

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
    	Field f = Table.class.getDeclaredField("etatTable");
    	f.setAccessible(true);

    	long hash = f.getLong(table);
    	Table cloned_table = table.clone();
        Assert.assertTrue(hash == f.getLong(cloned_table));
        cloned_table.setDone(GameElementNames.TRUC, Tribool.TRUE);
        Assert.assertTrue(hash != f.getLong(cloned_table));
    	cloned_table.copy(table);
    	hash = f.getLong(table);
        Assert.assertTrue(hash == f.getLong(cloned_table));
		Assert.assertTrue(table.isDone(GameElementNames.TRUC) == Tribool.TRUE);
		Assert.assertTrue(cloned_table.isDone(GameElementNames.TRUC) == Tribool.TRUE);
    	Assert.assertTrue(f.getLong(table) == f.getLong(cloned_table));
    	table.setDone(GameElementNames.MACHIN, Tribool.TRUE);
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
			table.setDone(e, Tribool.MAYBE);
			Assert.assertTrue(f.getLong(table) != hash);
			hash = f.getLong(table);
			table.setDone(e, Tribool.TRUE);
			Assert.assertTrue(f.getLong(table) != hash);
			hash = f.getLong(table);
		}
    }
}
