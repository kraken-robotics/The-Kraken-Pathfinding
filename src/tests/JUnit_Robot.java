package tests;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import robot.Robot;
import robot.RobotChrono;
import robot.RobotReal;
import smartMath.Vec2;
import enums.ConfigInfo;
import enums.PathfindingNodes;
import enums.ServiceNames;

/**
 * Tests unitaires sur Robot, RobotReal et RobotChrono
 * @author pf
 *
 */

public class JUnit_Robot extends JUnit_Test
{
	private RobotReal robotreal;
	private RobotChrono robotchrono;
	
    @Before
    public void setUp() throws Exception {
        super.setUp();
        robotreal = (RobotReal) container.getService(ServiceNames.ROBOT_REAL);
        robotchrono = robotreal.cloneIntoRobotChrono();
    }

    @Test
    public void test_symetrie() throws Exception
    {
    	for(int j = 0; j < 2; j++)
    	{
    		// TODO: debugger locomotion pour passer ce test
    		Robot robot;
//    		if(j == 0)
    			robot = robotchrono;
//    		else
//            	robot = robotreal;
	    	for(int i = 0; i < 2; i++)
	    	{
	    		if(i == 0)
	    			config.set(ConfigInfo.COULEUR, "vert");
	    		else
	    			config.set(ConfigInfo.COULEUR, "jaune");
	    		robot.updateConfig();
	    		robot.setPosition(new Vec2(200, 600));
	    		robot.setOrientation(0);
	    		robot.avancer(100, null);
	    		Assert.assertTrue(robot.getPosition().equals(new Vec2(300, 600)));
	    		robot.tourner(Math.PI/2);
	    		robot.avancer(100, null);
	    		Assert.assertTrue(robot.getPosition().equals(new Vec2(300, 700)));
	    		robot.tourner(Math.PI);
	    		robot.avancer(100, null);
	    		Assert.assertTrue(robot.getPosition().equals(new Vec2(200, 700)));
	    		robot.tourner(-Math.PI/2);
	    		robot.avancer(100, null);
	    		Assert.assertTrue(robot.getPosition().equals(new Vec2(200, 600)));
	    		ArrayList<PathfindingNodes> chemin = new ArrayList<PathfindingNodes>();
	    		chemin.add(PathfindingNodes.BAS);
	    		chemin.add(PathfindingNodes.DEVANT_DEPART_DROITE);
	    		robot.suit_chemin(chemin, null);
	    		Assert.assertTrue(robot.getPosition().equals(PathfindingNodes.DEVANT_DEPART_DROITE.getCoordonnees()));
	    	}
    	}
    }

}
