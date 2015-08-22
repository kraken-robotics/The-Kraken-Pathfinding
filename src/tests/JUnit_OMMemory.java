package tests;

import obstacles.ObstaclesIterator;
import obstacles.ObstaclesMemory;
import obstacles.types.ObstacleProximity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import permissions.ReadOnly;
import utils.ConfigInfo;
import utils.Sleep;
import utils.Vec2;
import container.ServiceNames;

public class JUnit_OMMemory extends JUnit_Test {

	ObstaclesMemory memory;
	ObstaclesIterator iterator;
	
    @Before
    public void setUp() throws Exception {
        super.setUp();
        memory = (ObstaclesMemory) container.getService(ServiceNames.OBSTACLES_MOBILES_MEMORY);
        iterator = new ObstaclesIterator(log, memory);
    }

    @Test
    public void test_iterator() throws Exception
    {
    	int peremption = config.getInt(ConfigInfo.DUREE_PEREMPTION_OBSTACLES);
    	iterator.reinitNow();
    	Assert.assertTrue(memory.getFirstNotDeadNow() == 0);
    	Assert.assertTrue(memory.getNextDeathDate() == Long.MAX_VALUE);

    	Assert.assertTrue(!iterator.hasNext());
    	long date = System.currentTimeMillis();
    	memory.add(new Vec2<ReadOnly>(1324,546), date);
    	Assert.assertTrue(memory.getFirstNotDeadNow() == 0);
    	iterator.reinitNow();

    	Assert.assertTrue(iterator.hasNext());    	
    	ObstacleProximity o = iterator.next();
    	Assert.assertTrue(o.position.x == 1324);
    	Assert.assertTrue(o.position.y == 546);
    	Assert.assertTrue(!iterator.hasNext());
    	Assert.assertTrue(memory.getFirstNotDeadNow() == 0);
    	memory.add(new Vec2<ReadOnly>(1324,546), date);
    	memory.add(new Vec2<ReadOnly>(1324,546), date);
    	memory.add(new Vec2<ReadOnly>(1324,546), date);
    	memory.add(new Vec2<ReadOnly>(1324,546), date);
    	memory.deleteOldObstacles();
    	Assert.assertTrue(memory.getFirstNotDeadNow() == 0);
    	Assert.assertTrue(memory.getNextDeathDate() == (date+peremption));
    	iterator.copy(iterator, date+peremption+10);
    	iterator.reinit();
    	Assert.assertTrue(!iterator.hasNext());
    	Sleep.sleep(peremption+10);
    	iterator.reinitNow();
    	Assert.assertTrue(memory.getFirstNotDeadNow() == 0);
    	Assert.assertTrue(memory.getNextDeathDate() == (date+peremption));
    	Assert.assertTrue(iterator.hasNext());
    	Assert.assertTrue(iterator.hasNext());
    	Assert.assertTrue(iterator.hasNext());
    	Assert.assertTrue(iterator.hasNext());
    	Assert.assertTrue(iterator.hasNext());
    	Assert.assertTrue(!iterator.hasNext());
    	memory.deleteOldObstacles();
    	iterator.reinitNow();
    	Assert.assertTrue(memory.getFirstNotDeadNow() == 5);
    	Assert.assertTrue(memory.size() == 5);
    	Assert.assertTrue(memory.getNextDeathDate() == Long.MAX_VALUE);
    	Assert.assertTrue(!iterator.hasNext());
    	
    }
}
