package tests;

import robot.RobotChrono;
import robot.RobotVrai;
import smartMath.Vec2;
import strategie.Strategie;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests unitaires de la stratégie
 * @author pf
 *
 */

public class JUnit_StrategieTest extends JUnit_Test {

	private RobotVrai robotvrai;
	private Strategie strategie;
//	private MemoryManager memorymanager;
//	private Pathfinding pathfinder;
//	private Table table;
	private RobotChrono robotchrono;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		robotvrai = (RobotVrai)container.getService("RobotVrai");
		strategie = (Strategie) container.getService("Strategie");
//		memorymanager = (MemoryManager) container.getService("MemoryManager");
//		pathfinder = (Pathfinding) container.getService("Pathfinding");
//		table = (Table) container.getService("Table");
		robotchrono = new RobotChrono(config, log);
	}

//	@Test
//	public void test_notescriptversion() throws Exception
//	{
//		NoteScriptVersion a = new NoteScriptVersion();
//		Assert.assertTrue(a.version == 0);
//		Assert.assertTrue(a.script == null);
//		Assert.assertTrue(a.note == 0);
//
//		ScriptManager scriptmanager = (ScriptManager)container.getService("ScriptManager");
//		Script s = (Script)scriptmanager.getScript("ScriptFresque");
//		a = new NoteScriptVersion(23, s, 12);
//		Assert.assertTrue(a.note == 23);
//		Assert.assertTrue(a.script == s);
//		Assert.assertTrue(a.version == 12);
//	}
 
	@Test
	public void test_evaluation() throws Exception
	{
		Vec2 initpos = new Vec2(1000,1300);
		robotvrai.setPosition(initpos);
		robotchrono.setPosition(initpos);
		
/*
		int testCount = 1000;
		long startTime = System.nanoTime();
		for (int i = 0; i < testCount; i++)
			memorymanager.getClone(1);
		long endTime = System.nanoTime();
		long duration = (endTime - startTime)/ (1000 * testCount);
		log.debug("Processed IA in " + duration + " µs on average over " + testCount + "tests", this);
		*/
		
		
		log.debug("Strategie Test starting", this);
		//strategie.evaluate();
		//log.debug("Strategie Test finished", this);
		
		for (int i = 0; i < 100; i++)
		{
			strategie.evaluate(null);
				
		}
		log.debug("Strategie Performance test starting", this);
		
		int testCount = 100000;
		long startTime = System.nanoTime();
		for (int i = 0; i < testCount; i++)
			strategie.evaluate(null);
		long endTime = System.nanoTime();
		long duration = (endTime - startTime)/ (1000 * testCount);
		log.debug("Processed IA in " + duration + " µs on average over " + testCount + "tests", this);
		
	}
}
