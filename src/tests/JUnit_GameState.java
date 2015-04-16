package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import permissions.ReadOnly;
import permissions.ReadWrite;
import container.ServiceNames;
import robot.RobotChrono;
import robot.RobotReal;
import strategie.GameState;
import table.GameElementNames;
import utils.Vec2;
import enums.Tribool;

/**
 * Tests unitaires pour le gamestate
 * @author pf
 *
 */

public class JUnit_GameState extends JUnit_Test {

	private GameState<RobotChrono,ReadWrite> gamestate;
	
    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        gamestate = GameState.cloneGameState(((GameState<RobotReal,ReadWrite>) container.getService(ServiceNames.REAL_GAME_STATE)).getReadOnly());
        GameState.setPosition(gamestate, new Vec2<ReadOnly>(1100, 1000));
    }

	@Test
	public void test_hash() throws Exception
	{
		// TODO
		// Sert à vérifier que les valeurs ne débordent pas les unes sur les autres
		// (si on alloue pas assez de bits par exemple)
/*		long hash = gamestate.getHash();
		Assert.assertEquals(GameState.getHashObstaclesMobiles(gamestate), (hash >> (16+(2*GameElementNames.values().length))));
		Assert.assertEquals(gamestate.gridspace.getHashTable(), (hash >> 16) % (1 << (2*GameElementNames.values().length)));
		Assert.assertEquals(gamestate.robot.areTapisPoses()?1:0, (hash >> 15) % (1 << 1));
		Assert.assertEquals((gamestate.robot.getPosition().x >> 2) % (1 << 3), (hash >> 12) % (1 << 3));
		Assert.assertEquals((gamestate.robot.getPosition().y >> 2) % (1 << 3), (hash >> 9) % (1 << 3));
		Assert.assertEquals(gamestate.robot.getPointsObtenus(), hash % (1 << 9));

		gamestate.robot.poserDeuxTapis();
		hash = gamestate.getHash();
		Assert.assertEquals(gamestate.gridspace.getHashObstaclesMobiles(), (hash >> (16+(2*GameElementNames.values().length))));
		Assert.assertEquals(gamestate.gridspace.getHashTable(), (hash >> 16) % (1 << (2*GameElementNames.values().length)));
		Assert.assertEquals(gamestate.robot.areTapisPoses()?1:0, (hash >> 15) % (1 << 1));
		Assert.assertEquals((gamestate.robot.getPosition().x >> 2) % (1 << 3), (hash >> 12) % (1 << 3));
		Assert.assertEquals((gamestate.robot.getPosition().y >> 2) % (1 << 3), (hash >> 9) % (1 << 3));
		Assert.assertEquals(gamestate.robot.getPointsObtenus(), hash % (1 << 9));

		gamestate.robot.setPositionPathfinding(PathfindingNodes.CLAP_DROIT);
		hash = gamestate.getHash();
		Assert.assertEquals(gamestate.gridspace.getHashObstaclesMobiles(), (hash >> (16+(2*GameElementNames.values().length))));
		Assert.assertEquals(gamestate.gridspace.getHashTable(), (hash >> 16) % (1 << (2*GameElementNames.values().length)));
		Assert.assertEquals(gamestate.robot.areTapisPoses()?1:0, (hash >> 15) % (1 << 1));
		Assert.assertEquals(gamestate.robot.getPositionPathfinding().ordinal(), (hash >> 9) % (1 << 6));
		Assert.assertEquals(gamestate.robot.getPointsObtenus(), hash % (1 << 9));
*/
	}

	@Test
	public void test_copy() throws Exception
	{
//		GameState.poserDeuxTapis(gamestate); // TODO
		GameState.avancer(gamestate, 200);
		// TODO: utiliser obstacle ennemi
//		GameState.creer_obstacle(gamestate, new Vec2<ReadOnly>(156, 282));
		GameState.setDone(gamestate, GameElementNames.CLAP_3, Tribool.TRUE);
		GameState.setDone(gamestate, GameElementNames.PLOT_4, Tribool.TRUE);
		Assert.assertEquals(gamestate.getHash(), GameState.cloneGameState(gamestate.getReadOnly()).getHash());
	}
}
