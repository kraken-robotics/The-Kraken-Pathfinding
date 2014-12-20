package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import robot.RobotChrono;
import robot.RobotReal;
import smartMath.Vec2;
import strategie.GameState;
import enums.GameElementNames;
import enums.PathfindingNodes;
import enums.ServiceNames;

public class JUnit_GameState extends JUnit_Test {

	private GameState<RobotChrono> gamestate;
	
    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        super.setUp();
        gamestate = ((GameState<RobotReal>) container.getService(ServiceNames.REAL_GAME_STATE)).cloneGameState();
        gamestate.robot.setPosition(new Vec2(1100, 1000));
        
    }

	@Test
	public void test_hash() throws Exception
	{
		long hash = gamestate.getHash();
		Assert.assertEquals(gamestate.gridspace.nbObstaclesMobiles(), (hash >> (16+(2*GameElementNames.values().length))));
		Assert.assertEquals(gamestate.robot.areTapisPoses()?1:0, (hash >> (15+(2*GameElementNames.values().length))) % (1 << 1));
		Assert.assertEquals((gamestate.robot.getPosition().x >> 2) % (1 << 3), (hash >> (12+(2*GameElementNames.values().length))) % (1 << 3));
		Assert.assertEquals((gamestate.robot.getPosition().y >> 2) % (1 << 3), (hash >> (9+(2*GameElementNames.values().length))) % (1 << 3));
		Assert.assertEquals(gamestate.gridspace.getHashTable(), (hash >> 9) % (1 << (2*GameElementNames.values().length)));
		Assert.assertEquals(gamestate.robot.getPointsObtenus(), hash % (1 << 9));

		gamestate.robot.poserDeuxTapis();
		hash = gamestate.getHash();
		Assert.assertEquals(gamestate.gridspace.nbObstaclesMobiles(), (hash >> (16+(2*GameElementNames.values().length))));
		Assert.assertEquals(gamestate.robot.areTapisPoses()?1:0, (hash >> (15+(2*GameElementNames.values().length))) % (1 << 1));
		Assert.assertEquals((gamestate.robot.getPosition().x >> 2) % (1 << 3), (hash >> (12+(2*GameElementNames.values().length))) % (1 << 3));
		Assert.assertEquals((gamestate.robot.getPosition().y >> 2) % (1 << 3), (hash >> (9+(2*GameElementNames.values().length))) % (1 << 3));
		Assert.assertEquals(gamestate.gridspace.getHashTable(), (hash >> 9) % (1 << (2*GameElementNames.values().length)));
		Assert.assertEquals(gamestate.robot.getPointsObtenus(), hash % (1 << 9));

		gamestate.robot.setPositionPathfinding(PathfindingNodes.CLAP_DROIT);
		hash = gamestate.getHash();
		Assert.assertEquals(gamestate.gridspace.nbObstaclesMobiles(), (hash >> (16+(2*GameElementNames.values().length))));
		Assert.assertEquals(gamestate.robot.areTapisPoses()?1:0, (hash >> (15+(2*GameElementNames.values().length))) % (1 << 1));
		Assert.assertEquals(gamestate.robot.getPositionPathfinding().ordinal(), (hash >> (9+(2*GameElementNames.values().length))) % (1 << 6));
		Assert.assertEquals(gamestate.gridspace.getHashTable(), (hash >> 9) % (1 << (2*GameElementNames.values().length)));
		Assert.assertEquals(gamestate.robot.getPointsObtenus(), hash % (1 << 9));

	}
	
}
