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
import enums.Tribool;

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
		// Sert à vérifier que les valeurs ne débordent pas les unes sur les autres
		// (si on alloue pas assez de bits par exemple)
		long hash = gamestate.getHash();
		Assert.assertEquals(gamestate.gridspace.getHashObstaclesMobiles(), (hash >> (16+(2*GameElementNames.values().length))));
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

	}
	
	@Test
	public void test_benchmark_copy() throws Exception
	{
		GameState<RobotChrono> gamestate2 = gamestate.cloneGameState();
		for(int i = 0; i < 100000; i++)
		{
			gamestate.robot.avancer(200);
			gamestate.gridspace.creer_obstacle(new Vec2(156, 282));
			gamestate.gridspace.setDone(GameElementNames.CLAP_3, Tribool.TRUE);
			gamestate2.copy(gamestate);
		}
	}

	@Test
	public void test_copy() throws Exception
	{
		gamestate.robot.poserDeuxTapis();
		gamestate.robot.avancer(200);
		gamestate.gridspace.creer_obstacle(new Vec2(156, 282));
		gamestate.gridspace.setDone(GameElementNames.CLAP_3, Tribool.TRUE);
		gamestate.gridspace.setDone(GameElementNames.PLOT_4, Tribool.TRUE);
		Assert.assertEquals(gamestate.getHash(), gamestate.cloneGameState().getHash());
	}
}
