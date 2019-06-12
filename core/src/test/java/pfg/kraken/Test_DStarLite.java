/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import pfg.kraken.dstarlite.DStarLite;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.struct.XY;
import pfg.kraken.struct.XYO;
import pfg.kraken.struct.XY_RW;

/**
 * Tests unitaires de la recherche de chemin.
 * 
 * @author pf
 *
 */

public class Test_DStarLite extends JUnit_Test
{

	private DStarLite pathfinding;

	@Before
	public void setUp() throws Exception
	{
		List<Obstacle> obs = new ArrayList<Obstacle>();
		obs.add(new RectangularObstacle(new XY_RW(50,1050), 500, 500));
		obs.add(new RectangularObstacle(new XY_RW(400,200), 200, 200));
		obs.add(new RectangularObstacle(new XY_RW(-1000,1050), 200, 200));
		obs.add(new RectangularObstacle(new XY_RW(100,410), 200, 200));
		obs.add(new RectangularObstacle(new XY_RW(-600,300), 200, 200));
		obs.add(new RectangularObstacle(new XY_RW(-1000,1900), 200, 200));
		super.setUpWith(obs, "default");
		pathfinding = injector.getService(DStarLite.class);
	}
	
	@Test
	public void test_chemin_dstarlite_statique() throws Exception
	{
		pathfinding.computeNewPath(new XY(-800, 200), new XY(1200, 1200), true, true);
		List<XYO> l = pathfinding.itineraireBrut(new XY(-800, 200));
		for(XYO pos : l)
			System.out.println(pos);
	}	
/*
	@Test
	public void test_chemin_dstarlite() throws Exception
	{
		gridspace.addObstacleAndRemoveNearbyObstacles(new ObstacleCircular(new Vec2RO(100, 1100), 200));
		pathfinding.computeNewPath(new Vec2RO(-800, 200), new Vec2RO(1200, 1200));
		pathfinding.itineraireBrut();
		Thread.sleep(500);
		log.debug("RECALCUL");
		gridspace.addObstacleAndRemoveNearbyObstacles(new ObstacleCircular(new Vec2RO(600, 1300), 200));
		pathfinding.updateStart(new Vec2RO(600, 1300));
		pathfinding.updateObstaclesEnnemi();
		pathfinding.itineraireBrut();
		Thread.sleep(4000);
		pathfinding.updateStart(new Vec2RO(-1100, 1300));
		pathfinding.updateObstaclesEnnemi();
		pathfinding.itineraireBrut();
		log.debug("RECALCUL");
	}

	public void test_stress() throws Exception
	{
		long dateAvant = System.currentTimeMillis();
		int nbBoucle = 100000;
		for(int i = 0; i < nbBoucle; i++)
		{
			pathfinding.computeNewPath(new Vec2RO(-1000, 200), new Vec2RO(1200, 1200));
			gridspace.addObstacleAndRemoveNearbyObstacles(new ObstacleCircular(new Vec2RO(100, 700), 200));
			pathfinding.updateStart(new Vec2RO(600, 1300));
			pathfinding.updateObstaclesEnnemi();
			gridspace.addObstacleAndRemoveNearbyObstacles(new ObstacleCircular(new Vec2RO(500, 1200), 200));
			pathfinding.updateStart(new Vec2RO(-800, 1300));
			pathfinding.updateObstaclesEnnemi();
		}
		log.debug("En moyenne : " + ((System.currentTimeMillis() - dateAvant) / (3. * nbBoucle)));
	}

	@Test
	public void test_exception1() throws Exception
	{
		pathfinding.computeNewPath(new Vec2RO(-1000, 200), new Vec2RO(1200, 1200));
		gridspace.addObstacleAndRemoveNearbyObstacles(new ObstacleCircular(new Vec2RO(1100, 1200), 200));
		pathfinding.updateStart(new Vec2RO(-900, 1400));
		pathfinding.updateObstaclesEnnemi();
		Assert.assertEquals(null, pathfinding.itineraireBrut());
	}
	*/
// ne fonctionne plus car la valeur de DISTANCE_BETWEEN_PROXIMITY_OBSTACLES a changé
/*	@Test
	public void test_exception2() throws Exception
	{
		pathfinding.computeNewPath(new Vec2RO(-1000, 300), new Vec2RO(0, 1800), false);
		gridspace.addObstacleAndRemoveNearbyObstacles(new ObstacleCircular(new Vec2RO(200, 1800), 200));
		gridspace.addObstacleAndRemoveNearbyObstacles(new ObstacleCircular(new Vec2RO(200, 1600), 200));
		gridspace.addObstacleAndRemoveNearbyObstacles(new ObstacleCircular(new Vec2RO(0, 1600), 200));
		pathfinding.updateStart(new Vec2RO(-900, 300));
		pathfinding.updateObstaclesEnnemi();
		Assert.assertEquals(null, pathfinding.itineraireBrut());
	}*/
/*
	@Test
	public void test_chemin_impossible() throws Exception
	{
		gridspace.addObstacleAndRemoveNearbyObstacles(new ObstacleCircular(new Vec2RO(1000, 1200), 200));
		pathfinding.computeNewPath(new Vec2RO(-1000, 400), new Vec2RO(1000, 1200));
		Assert.assertEquals(null, pathfinding.itineraireBrut());
	}

	@Test
	public void test_ajout_proche() throws Exception
	{
		Vec2RO posRobot = new Vec2RO(0, 1200);
		gridspace.addObstacleAndRemoveNearbyObstacles(new ObstacleCircular(new Vec2RO(1000, 1200), 200));
		pathfinding.computeNewPath(posRobot, new Vec2RO(1000, 600));
		gridspace.addObstacleAndRemoveNearbyObstacles(new ObstacleCircular(new Vec2RO(900, 1200), 200));
		pathfinding.updateStart(new Vec2RO(-900, 1000));
		pathfinding.updateObstaclesEnnemi();
		pathfinding.itineraireBrut();
	}

	@Test
	public void test_simulation_pathfinding() throws Exception
	{
		Vec2RO posRobot = new Vec2RO(-1100, 1300);
		pathfinding.computeNewPath(posRobot, new Vec2RO(600, 1100));
		List<Vec2RO> chemin = pathfinding.itineraireBrut();

		int n = 20;
		// while(n+20 < chemin.size())
		{
			Thread.sleep(2000);
			posRobot = chemin.get(n);
			gridspace.addObstacleAndRemoveNearbyObstacles(new ObstacleCircular(chemin.get(n + 20), 200));
			pathfinding.updateStart(posRobot);
			pathfinding.updateObstaclesEnnemi();
			chemin = pathfinding.itineraireBrut();
		}
	}*/
}
