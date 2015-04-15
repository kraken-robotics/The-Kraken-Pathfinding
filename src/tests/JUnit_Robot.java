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
import robot.RobotChrono;
import robot.RobotReal;
import strategie.GameState;
import utils.ConfigInfo;
import vec2.ReadOnly;
import vec2.ReadWrite;
import vec2.Vec2;

/**
 * Tests unitaires sur Robot, RobotReal et RobotChrono
 * @author pf
 *
 */

public class JUnit_Robot extends JUnit_Test
{
	private GameState<RobotReal,ReadWrite> state;
	private GameState<RobotChrono,ReadWrite> chronostate;
	
    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        state = (GameState<RobotReal,ReadWrite>) container.getService(ServiceNames.REAL_GAME_STATE);
        chronostate = GameState.cloneGameState(state.getReadOnly());
    }

    @Test
    public void test_symetrie() throws Exception
    {
    	for(int j = 0; j < 2; j++)
    	{
    		GameState<?, ReadWrite> gamestate;
    		if(j == 0)
    			gamestate = chronostate;
    		else
    			gamestate = state;
	    	for(int i = 0; i < 2; i++)
	    	{
	    		if(i == 0)
	    			config.set(ConfigInfo.COULEUR, RobotColor.getCouleurSansSymetrie());
	    		else
	    			config.set(ConfigInfo.COULEUR, RobotColor.getCouleurAvecSymetrie());
	    		gamestate.updateConfig();
	    		GameState.setPosition(gamestate, new Vec2<ReadOnly>(200, 600));
	    		GameState.setOrientation(gamestate, 0);
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
	    		ArrayList<SegmentTrajectoireCourbe> chemin = new ArrayList<SegmentTrajectoireCourbe>();
	    		chemin.add(new SegmentTrajectoireCourbe(PathfindingNodes.BAS));
	    		chemin.add(new SegmentTrajectoireCourbe(PathfindingNodes.DEVANT_DEPART_DROITE));
	    		GameState.suit_chemin(gamestate, chemin, new ArrayList<Hook>());
	    		Assert.assertTrue(GameState.getPosition(gamestate.getReadOnly()).squaredDistance(PathfindingNodes.DEVANT_DEPART_DROITE.getCoordonnees()) < 10);
	    	}
    	}
    }

}
