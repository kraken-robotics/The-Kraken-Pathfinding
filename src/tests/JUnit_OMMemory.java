package tests;

import obstacles.memory.ObstaclesIteratorPresent;
import obstacles.memory.ObstaclesMemory;
import obstacles.types.Obstacle;
import obstacles.types.ObstacleProximity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import utils.ConfigInfo;
import utils.Vec2RO;
import utils.Vec2RW;

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
        memory = container.getService(ObstaclesMemory.class);
        iterator = new ObstaclesIteratorPresent(log, memory);
    }

	@Test
    public void test_iterator() throws Exception
    {
    	Field f = Obstacle.class.getDeclaredField("position");
    	f.setAccessible(true);

    	Method m = ObstaclesMemory.class.getDeclaredMethod("add", Vec2RO.class, long.class, ArrayList.class);
    	m.setAccessible(true);
    	
    	int peremption = config.getInt(ConfigInfo.DUREE_PEREMPTION_OBSTACLES);
    	iterator.reinit();
    	Assert.assertTrue(memory.getFirstNotDeadNow() == 0);
    	Assert.assertTrue(memory.getNextDeathDate() == Long.MAX_VALUE);

    	
    	
    	long date = System.currentTimeMillis();
    	m.invoke(memory, new Vec2RO(1324,546), date, null);
    	m.invoke(memory, new Vec2RO(1324,546), date, null);
    	iterator.reinit();
    	Assert.assertTrue(iterator.hasNext());
    	iterator.next();
    	iterator.remove();
    	Assert.assertTrue(iterator.hasNext());
    	iterator.next();
    	Assert.assertTrue(!iterator.hasNext());
    	iterator.remove();

    	m.invoke(memory, new Vec2RO(1324,546), date, null);
    	Assert.assertTrue(memory.getFirstNotDeadNow() == 0);
    	iterator.reinit();

    	Assert.assertTrue(iterator.hasNext());    	
    	ObstacleProximity o = iterator.next();    	
    	Assert.assertTrue(((Vec2RW)f.get(o)).getX() == 1324);
    	Assert.assertTrue(((Vec2RW)f.get(o)).getY() == 546);
    	Assert.assertTrue(!iterator.hasNext());
    	Assert.assertTrue(memory.getFirstNotDeadNow() == 0);
    	m.invoke(memory, new Vec2RO(1324,546), date, null);
    	m.invoke(memory, new Vec2RO(1324,546), date, null);
    	m.invoke(memory, new Vec2RO(1324,546), date, null);
    	m.invoke(memory, new Vec2RO(1324,546), date, null);
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
