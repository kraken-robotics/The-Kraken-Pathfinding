package tests;

import hook.Hook;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import astar.arc.PathfindingNodes;
import astar.arc.SegmentTrajectoireCourbe;
import container.ServiceNames;
import enums.RobotColor;
import robot.Robot;
import robot.RobotChrono;
import robot.RobotReal;
import strategie.GameState;
import utils.ConfigInfo;
import vec2.ReadWrite;
import vec2.Vec2;

/**
 * Tests unitaires sur Robot, RobotReal et RobotChrono
 * @author pf
 *
 */

public class JUnit_Robot extends JUnit_Test
{
	private GameState<RobotReal> state;
	private GameState<RobotChrono> chronostate;
	
    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        state = (GameState<RobotReal>) container.getService(ServiceNames.REAL_GAME_STATE);
        chronostate = state.cloneGameState();
    }

    @Test
    public void test_symetrie() throws Exception
    {
    	for(int j = 0; j < 2; j++)
    	{
    		Robot robot;
    		if(j == 0)
    			robot = chronostate.robot;
    		else
            	robot = state.robot;
	    	for(int i = 0; i < 2; i++)
	    	{
	    		if(i == 0)
	    			config.set(ConfigInfo.COULEUR, RobotColor.getCouleurSansSymetrie());
	    		else
	    			config.set(ConfigInfo.COULEUR, RobotColor.getCouleurAvecSymetrie());
	    		robot.updateConfig();
	    		robot.setPosition(new Vec2<ReadWrite>(200, 600));
	    		robot.setOrientation(0);
	    		robot.avancer(100);
	    		Assert.assertTrue(robot.getPosition().squaredDistance(new Vec2<ReadWrite>(300, 600)) < 10);
	    		robot.tourner(Math.PI/2);
	    		robot.avancer(100);
	    		Assert.assertTrue(robot.getPosition().squaredDistance(new Vec2<ReadWrite>(300, 700)) < 10);
	    		robot.tourner(Math.PI);
	    		robot.avancer(100);
	    		Assert.assertTrue(robot.getPosition().squaredDistance(new Vec2<ReadWrite>(200, 700)) < 10);
	    		robot.tourner(-Math.PI/2);
	    		robot.avancer(100);
	    		Assert.assertTrue(robot.getPosition().squaredDistance(new Vec2<ReadWrite>(200, 600)) < 10);
	    		ArrayList<SegmentTrajectoireCourbe> chemin = new ArrayList<SegmentTrajectoireCourbe>();
	    		chemin.add(new SegmentTrajectoireCourbe(PathfindingNodes.BAS));
	    		chemin.add(new SegmentTrajectoireCourbe(PathfindingNodes.DEVANT_DEPART_DROITE));
	    		robot.suit_chemin(chemin, new ArrayList<Hook>());
	    		Assert.assertTrue(robot.getPosition().squaredDistance(PathfindingNodes.DEVANT_DEPART_DROITE.getCoordonnees()) < 10);
	    	}
    	}
    }

}
