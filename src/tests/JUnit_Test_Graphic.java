package tests;

import java.util.ArrayList;
import java.util.Random;

import obstacles.ObstacleManager;

import org.junit.Before;
import org.junit.Test;

import pathfinding.GridSpace;
import pathfinding.Pathfinding;
import enums.PathfindingNodes;
import enums.ServiceNames;
import tests.graphicLib.Fenetre;
import utils.Sleep;

public class JUnit_Test_Graphic extends JUnit_Test {

	Fenetre fenetre;
	ObstacleManager obstaclemanager;
	private Pathfinding pathfinding;
	private GridSpace gridspace;

	@Before
	public void setUp() throws Exception
	{
		super.setUp();
        pathfinding = (Pathfinding) container.getService(ServiceNames.PATHFINDING);
		gridspace = (GridSpace) container.getService(ServiceNames.GRID_SPACE);
		obstaclemanager = (ObstacleManager) container.getService(ServiceNames.OBSTACLE_MANAGER);
		fenetre = new Fenetre();
		fenetre.setDilatationObstacle(obstaclemanager.getDilatationObstacle());
		for(PathfindingNodes n : PathfindingNodes.values())
		{
			fenetre.addPoint(n.getCoordonnees());
			for(PathfindingNodes m : PathfindingNodes.values())
				if(!obstaclemanager.obstacle_fixe_dans_segment_pathfinding(n.getCoordonnees(), m.getCoordonnees()))
						fenetre.addSegment(m.getCoordonnees(), n.getCoordonnees());
		}
		updateAffichage();
		fenetre.showOnFrame();
	}
	
	public void updateAffichage()
	{
		fenetre.setGameElement(obstaclemanager.getListGameElement());
		fenetre.setObstaclesFixes(obstaclemanager.getListObstaclesFixes());
		fenetre.setObstaclesMobiles(obstaclemanager.getListObstaclesMobiles());		
	}

	@Test
    public void test_verification_humaine() throws Exception
    {
		gridspace.setAvoidGameElement(false);
		Random randomgenerator = new Random();
		for(int k = 0; k < 10; k++)
		{
			PathfindingNodes i = PathfindingNodes.values()[randomgenerator.nextInt(PathfindingNodes.values().length)];
			PathfindingNodes j = PathfindingNodes.values()[randomgenerator.nextInt(PathfindingNodes.values().length)];
			log.debug("Recherche chemin entre "+i+" et "+j, this);
    		ArrayList<PathfindingNodes> chemin = pathfinding.computePath(i.getCoordonnees(), j, gridspace);
    		fenetre.setPath(chemin);
    		fenetre.repaint();
    		Sleep.sleep(2000);
		}
    }
	

}
