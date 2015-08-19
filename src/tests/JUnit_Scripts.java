package tests;

import org.junit.Before;

import permissions.ReadOnly;
import permissions.ReadWrite;
import container.ServiceNames;
import robot.RobotReal;
import strategie.GameState;
import utils.Vec2;

/**
 * Tests unitaires des scripts.
 * Utilisé pour voir en vrai comment agit le robot et si la table est bien mise à jour.
 * @author pf
 *
 */

public class JUnit_Scripts extends JUnit_Test {

	private GameState<RobotReal,ReadWrite> gamestate;

    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        gamestate = (GameState<RobotReal,ReadWrite>) container.getService(ServiceNames.REAL_GAME_STATE);
        GameState.setPositionOrientationSTM(gamestate, new Vec2<ReadOnly>(1100, 1000), 0);
    }

}
