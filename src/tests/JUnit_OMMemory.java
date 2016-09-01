package tests;

import obstacles.memory.ObstaclesIteratorPresent;
import obstacles.memory.ObstaclesMemory;
import obstacles.types.ObstacleProximity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import utils.ConfigInfo;
import utils.Vec2;
import utils.permissions.ReadOnly;
import container.ServiceNames;

/**
 * Tests unitaires de l'obstacle memory manager
 * @author pf
 *
 */

public class JUnit_OMMemory extends JUnit_Test {

	ObstaclesMemory memory;
	ObstaclesIteratorPresent iterator;
	
    @Override
	@Before
    public void setUp() throws Exception {
        super.setUp();
        memory = (ObstaclesMemory) container.getService(ServiceNames.OBSTACLES_MEMORY);
        iterator = new ObstaclesIteratorPresent(log, memory);
    }

    @Test
    public void test_iterator() throws Exception
    {
    	int peremption = config.getInt(ConfigInfo.DUREE_PEREMPTION_OBSTACLES);
    	iterator.reinit();
    	Assert.assertTrue(memory.getFirstNotDeadNow() == 0);
    	Assert.assertTrue(memory.getNextDeathDate() == Long.MAX_VALUE);

    	Assert.assertTrue(!iterator.hasNext());
    	long date = System.currentTimeMillis();
    	memory.add(new Vec2<ReadOnly>(1324,546), date, null);
    	Assert.assertTrue(memory.getFirstNotDeadNow() == 0);
    	iterator.reinit();

    	Assert.assertTrue(iterator.hasNext());    	
    	ObstacleProximity o = iterator.next();
    	Assert.assertTrue(o.position.x == 1324);
    	Assert.assertTrue(o.position.y == 546);
    	Assert.assertTrue(!iterator.hasNext());
    	Assert.assertTrue(memory.getFirstNotDeadNow() == 0);
    	memory.add(new Vec2<ReadOnly>(1324,546), date, null);
    	memory.add(new Vec2<ReadOnly>(1324,546), date, null);
    	memory.add(new Vec2<ReadOnly>(1324,546), date, null);
    	memory.add(new Vec2<ReadOnly>(1324,546), date, null);
    	memory.deleteOldObstacles();
    	Assert.assertTrue(memory.getFirstNotDeadNow() == 0);
    	Assert.assertTrue(memory.getNextDeathDate() == (date+peremption));
    	Thread.sleep(peremption+10);
//    	iterator.reinitNow();
//    	Assert.assertTrue(memory.getFirstNotDeadNow() == 0);
//    	Assert.assertTrue(memory.getNextDeathDate() == (date+peremption));
//    	Assert.assertTrue(iterator.hasNext());
//    	memory.deleteOldObstacles();
    	iterator.reinit();
    	Assert.assertTrue(memory.getFirstNotDeadNow() == 5);
    	Assert.assertTrue(memory.size() == 5);
    	Assert.assertTrue(memory.getNextDeathDate() == Long.MAX_VALUE);
    	Assert.assertTrue(!iterator.hasNext());
    	
    }
}
