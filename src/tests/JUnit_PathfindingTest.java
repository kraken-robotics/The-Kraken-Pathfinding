package tests;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import pathfinding.Pathfinding;
import robot.RobotVrai;
import smartMath.Vec2;

// TODO

/**
 * Tests du pathfinding
 * @author pf
 *
 */

public class JUnit_PathfindingTest extends JUnit_Test 
{
	
	Pathfinding pathfinding;
	RobotVrai robotvrai;
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		pathfinding = (Pathfinding) container.getService("Pathfinding");
		robotvrai = (RobotVrai) container.getService("RobotVrai");
		robotvrai.setPosition(new Vec2(1000, 900));
		robotvrai.setOrientation(Math.PI/2);
	}
	
	@Test
	public void itineraire_test() throws Exception
	{
		System.out.println(pathfinding.cheminAStar(new Vec2(-500,1300), new Vec2(1000, 500)).size());
		Assert.assertTrue(pathfinding.cheminAStar(new Vec2(-500,1300), new Vec2(1000, 500)).size() > 1);
	}
	
    @Test
    public void pathfinding_test() throws Exception
    {
        robotvrai.setPosition(new Vec2(250, 500));
        ArrayList<Vec2> chemin = pathfinding.chemin(robotvrai.getPosition(), new Vec2(-700, 1450));
        for(Vec2 point: chemin)
            System.out.println(point);
        robotvrai.suit_chemin(chemin, null);
        Assert.assertTrue(robotvrai.getPosition().distance(new Vec2(-700, 1450)) < 10);
    }

/*	@Test
	public void long_itineraire_test() throws Exception
	{
		Random randomgenerator = new Random();
		Vec2 arrivee;
		robotvrai.setPosition(new Vec2(0, 1400));
		while(true)
		{
			arrivee = new Vec2((Math.abs(randomgenerator.nextInt())%3000)-1500, Math.abs(randomgenerator.nextInt())%2000);
			log.debug("Depart: "+robotvrai.getPosition()+", arrivée: "+arrivee, this);
			try {
				robotvrai.va_au_point_pathfinding(pathfinding, arrivee, null, false);
			}
			catch(Exception e)
			{
				log.critical(e, this);
			}
		}
	}
	
*/
/*	@Test
	public void performanceTest() throws Exception
	{
		Random randomgenerator = new Random();
		Vec2 arrivee, depart;
		robotvrai.setPosition(new Vec2(0, 1400));

		log.debug("Simple pathfinding Performance test starting", this);
		int testCount = 1000;
		long duration = 0;
		for (int i = 0; i < testCount; i++)
		{
			arrivee = new Vec2((Math.abs(randomgenerator.nextInt())%3000)-1500, Math.abs(randomgenerator.nextInt())%2000);
			depart = robotvrai.getPosition();
			//log.debug("Depart: "+robotvrai.getPosition()+", arrivée: "+arrivee, this);
			long startTime = System.nanoTime();
			try {
				pathfinding.chemin(depart, arrivee);
			}
			catch(Exception e)
			{
				log.critical(e, this);
			}
			long endTime = System.nanoTime();
			duration += (endTime - startTime);

			
		}
		log.debug("Processed simple pathfinding in " + duration / (1000* testCount) + " µs on average over " + testCount + "tests", this);
		
		
		
		
	}
*/
}
