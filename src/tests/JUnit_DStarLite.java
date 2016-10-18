/*
Copyright (C) 2016 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package tests;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import exceptions.PathfindingException;
import pathfinding.dstarlite.DStarLite;
import pathfinding.dstarlite.gridspace.GridSpace;
import utils.Vec2RO;

/**
 * Tests unitaires de la recherche de chemin.
 * @author pf
 *
 */

public class JUnit_DStarLite extends JUnit_Test {

	private DStarLite pathfinding;
	private GridSpace gridspace;
	
	@Override
	@Before
    public void setUp() throws Exception {
        super.setUp();
        pathfinding = container.getService(DStarLite.class);
        gridspace = container.getService(GridSpace.class);
	}
	
	@Test
    public void test_chemin_dstarlite() throws Exception
    {
		gridspace.addObstacleAndRemoveNearbyObstacles(new Vec2RO(100, 700));
		pathfinding.computeNewPath(new Vec2RO(-1000, 200), new Vec2RO(1200, 1200));
		pathfinding.itineraireBrut();		
		Thread.sleep(500);
		log.debug("RECALCUL");
		gridspace.addObstacleAndRemoveNearbyObstacles(new Vec2RO(600, 1300));
		pathfinding.updateStart(new Vec2RO(600,1300));
		pathfinding.updateObstacles();
		pathfinding.itineraireBrut();
		Thread.sleep(4000);
		pathfinding.updateStart(new Vec2RO(-1100,1300));
		pathfinding.updateObstacles();
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
			gridspace.addObstacleAndRemoveNearbyObstacles(new Vec2RO(100, 700));
			pathfinding.updateStart(new Vec2RO(600,1300));
			pathfinding.updateObstacles();
			gridspace.addObstacleAndRemoveNearbyObstacles(new Vec2RO(500, 1200));
			pathfinding.updateStart(new Vec2RO(-800,1300));
			pathfinding.updateObstacles();
		}
		log.debug("En moyenne : "+((System.currentTimeMillis() - dateAvant)/(3.*nbBoucle)));
    }
	
	@Test(expected = PathfindingException.class)
    public void test_exception1() throws Exception
    {
		pathfinding.computeNewPath(new Vec2RO(-1000, 200), new Vec2RO(1200, 1200));
		gridspace.addObstacleAndRemoveNearbyObstacles(new Vec2RO(1100, 1200));
		pathfinding.updateStart(new Vec2RO(-900, 1400));
		pathfinding.updateObstacles();
		pathfinding.itineraireBrut();
    }

	@Test(expected = PathfindingException.class)
    public void test_exception2() throws Exception
    {
		pathfinding.computeNewPath(new Vec2RO(-1000, 200), new Vec2RO(1300, 1800));
		gridspace.addObstacleAndRemoveNearbyObstacles(new Vec2RO(1100, 1800));
		gridspace.addObstacleAndRemoveNearbyObstacles(new Vec2RO(1100, 1600));
		gridspace.addObstacleAndRemoveNearbyObstacles(new Vec2RO(1300, 1600));
		pathfinding.updateStart(new Vec2RO(-900, 1400));
		pathfinding.updateObstacles();
		pathfinding.itineraireBrut();
    }

	@Test(expected = PathfindingException.class)
    public void test_chemin_impossible() throws Exception
    {
		gridspace.addObstacleAndRemoveNearbyObstacles(new Vec2RO(1000, 1200));
		pathfinding.computeNewPath(new Vec2RO(-1000, 400), new Vec2RO(1000, 1200));
		pathfinding.itineraireBrut();
    }
	
	@Test
    public void test_ajout_proche() throws Exception
    {
		Vec2RO posRobot = new Vec2RO(0, 1200);
		gridspace.addObstacleAndRemoveNearbyObstacles(new Vec2RO(1000, 1200));
		pathfinding.computeNewPath(posRobot, new Vec2RO(1000, 600));
		gridspace.addObstacleAndRemoveNearbyObstacles(new Vec2RO(900, 1200));
		pathfinding.updateStart(new Vec2RO(-900, 1000));
		pathfinding.updateObstacles();
		pathfinding.itineraireBrut();
    }

	@Test
    public void test_simulation_pathfinding() throws Exception
    {
		Vec2RO posRobot = new Vec2RO(-1100, 1300);
		pathfinding.computeNewPath(posRobot, new Vec2RO(1100, 500));
		ArrayList<Vec2RO> chemin = pathfinding.itineraireBrut();
		
		int n = 15;
		while(n+6 < chemin.size())
		{
			Thread.sleep(1000);
			posRobot = chemin.get(n);
			gridspace.addObstacleAndRemoveNearbyObstacles(chemin.get(n+6));
			pathfinding.updateStart(posRobot);
			pathfinding.updateObstacles();
			chemin = pathfinding.itineraireBrut();
		}
    }
}
