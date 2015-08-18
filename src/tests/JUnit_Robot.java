package tests;

import hook.Hook;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import permissions.ReadOnly;
import permissions.ReadWrite;
import planification.LocomotionArc;
import planification.astar.arc.PathfindingNodes;
import container.ServiceNames;
import enums.RobotColor;
import robot.ActuatorOrder;
import robot.RobotChrono;
import robot.RobotReal;
import robot.Speed;
import strategie.GameState;
import utils.ConfigInfo;
import utils.Vec2;

/**
 * Tests unitaires sur Robot, RobotReal et RobotChrono
 * @author pf
 *
 */

public class JUnit_Robot extends JUnit_Test
{
	private GameState<RobotReal,ReadWrite> state;
	private GameState<RobotChrono,ReadWrite> chronostate;
	private RobotReal robot;
	
    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        state = (GameState<RobotReal,ReadWrite>) container.getService(ServiceNames.REAL_GAME_STATE);
        chronostate = GameState.cloneGameState(state.getReadOnly());
        robot = (RobotReal) container.getService(ServiceNames.ROBOT_REAL);
    }

    @Test
    public void test_stop() throws Exception
    {
    	robot.stopper();
    }

	@Test
	public void test_actionneurs() throws Exception
	{
		for(ActuatorOrder o: ActuatorOrder.values())
			robot.useActuator(o);
	}

    @Test
    public void test_avance() throws Exception
    {
    	robot.avancer(100);
    	log.debug("On est arrivé");
    }

    @Test
    public void test_tourne() throws Exception
    {
    	robot.tourner(Math.PI);
    }

    @Test
    public void test_set_pos_orientation() throws Exception
    {
    	robot.tourner(Math.PI);
    }

    @Test
    public void test_speed() throws Exception
    {
    	robot.set_vitesse(Speed.BETWEEN_SCRIPTS);
    }

    @Test
    public void test_symetrie() throws Exception
    {
		GameState<RobotChrono, ReadWrite> gamestate = chronostate;
    	for(int i = 0; i < 2; i++)
    	{
    		if(i == 0)
    			config.set(ConfigInfo.COULEUR, RobotColor.getCouleurSansSymetrie());
    		else
    			config.set(ConfigInfo.COULEUR, RobotColor.getCouleurAvecSymetrie());
    		GameState.setPositionOrientationSTM(gamestate, new Vec2<ReadOnly>(200, 600), 0);
    		GameState.avancer(gamestate, 100);
    		Assert.assertTrue(GameState.getPosition(gamestate.getReadOnly()).squaredDistance(new Vec2<ReadWrite>(300, 600)) < 10);
    		GameState.tourner(gamestate, Math.PI/2);
    		GameState.avancer(gamestate, 100);
    		Assert.assertTrue(GameState.getPosition(gamestate.getReadOnly()).squaredDistance(new Vec2<ReadWrite>(300, 700)) < 10);
    		GameState.tourner(gamestate, Math.PI);
    		GameState.avancer(gamestate, 100);
    		Assert.assertTrue(GameState.getPosition(gamestate.getReadOnly()).squaredDistance(new Vec2<ReadWrite>(200, 700)) < 10);
    		GameState.tourner(gamestate, -Math.PI/2);
    		GameState.avancer(gamestate, 100);
    		Assert.assertTrue(GameState.getPosition(gamestate.getReadOnly()).squaredDistance(new Vec2<ReadWrite>(200, 600)) < 10);
    		ArrayList<LocomotionArc> chemin = new ArrayList<LocomotionArc>();
//	    		chemin.add(new SegmentTrajectoireCourbe(PathfindingNodes.BAS));
//	    		chemin.add(new SegmentTrajectoireCourbe(PathfindingNodes.DEVANT_DEPART_DROITE));
    		GameState.suit_chemin(gamestate, chemin, new ArrayList<Hook>());
    		Assert.assertTrue(GameState.getPosition(gamestate.getReadOnly()).squaredDistance(PathfindingNodes.DEVANT_DEPART_DROITE.getCoordonnees()) < 10);
    	}
    }

}
