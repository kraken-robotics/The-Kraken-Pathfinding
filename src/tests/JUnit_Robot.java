package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import container.ServiceNames;
import enums.RobotColor;
import exceptions.FinMatchException;
import robot.RobotChrono;
import robot.RobotReal;
import robot.Speed;
import utils.ConfigInfo;
import utils.Vec2;
import utils.permissions.ReadOnly;
import utils.permissions.ReadWrite;

/**
 * Tests unitaires sur Robot, RobotReal et RobotChrono
 * @author pf
 *
 */

public class JUnit_Robot extends JUnit_Test
{
	private RobotReal robot;
	private RobotChrono robotchrono;
	
	@Before
    public void setUp() throws Exception {
        super.setUp();
        robot = (RobotReal) container.getService(ServiceNames.ROBOT_REAL);
        robotchrono = robot.cloneIntoRobotChrono();
	}
    /*
    @Test
    public void test_symetrie_robot_chrono() throws Exception
    {
    	for(int i = 0; i < 2; i++)
    	{
    		if(i == 0)
    			config.set(ConfigInfo.COULEUR, RobotColor.getCouleurSansSymetrie());
    		else
    			config.set(ConfigInfo.COULEUR, RobotColor.getCouleurAvecSymetrie());
// TODO tests robot
    		//    		robotchrono.setPositionOrientationSTM(new Vec2<ReadOnly>(200, 600), 0);
    		robotchrono.avancer(100, false, Speed.STANDARD);
    		Assert.assertTrue(robotchrono.getCinematique().getPosition().squaredDistance(new Vec2<ReadWrite>(300, 600)) < 10);
    		robotchrono.avancer(100, false, Speed.STANDARD);
    		Assert.assertTrue(robotchrono.getCinematique().getPosition().squaredDistance(new Vec2<ReadWrite>(300, 700)) < 10);
    		robotchrono.avancer(100, false, Speed.STANDARD);
    		Assert.assertTrue(robotchrono.getCinematique().getPosition().squaredDistance(new Vec2<ReadWrite>(200, 700)) < 10);
    		robotchrono.avancer(100, false, Speed.STANDARD);
    		Assert.assertTrue(robotchrono.getCinematique().getPosition().squaredDistance(new Vec2<ReadWrite>(200, 600)) < 10);
//    		ArrayList<LocomotionArc> chemin = new ArrayList<LocomotionArc>();
//	    		chemin.add(new SegmentTrajectoireCourbe(PathfindingNodes.BAS));
//	    		chemin.add(new SegmentTrajectoireCourbe(PathfindingNodes.DEVANT_DEPART_DROITE));
//    		robotchrono.suit_chemin(chemin, new ArrayList<Hook>());
//    		Assert.assertTrue(robotchrono.getPosition(robotchrono.getReadOnly()).squaredDistance(PathfindingNodes.DEVANT_DEPART_DROITE.getCoordonnees()) < 10);
    	}
    }*/

}
