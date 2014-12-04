package tests;

import obstacles.ObstacleManager;

import org.junit.Before;

import enums.PathfindingNodes;
import enums.ServiceNames;
import tests.graphicLib.Fenetre;

public class JUnit_Test_Graphic extends JUnit_Test {

	Fenetre fenetre;
	ObstacleManager obstaclemanager;
	
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		obstaclemanager = (ObstacleManager) container.getService(ServiceNames.OBSTACLE_MANAGER);
		fenetre = new Fenetre();
		fenetre.setDilatationObstacle(obstaclemanager.getDilatationObstacle());
		for(PathfindingNodes n : PathfindingNodes.values())
			fenetre.addPoint(n.getCoordonnees());
		fenetre.repaint();
		updateAffichage();
		fenetre.showOnFrame();
	}
	
	public void updateAffichage()
	{
		fenetre.setGameElement(obstaclemanager.getListGameElement());
		fenetre.setObstaclesFixes(obstaclemanager.getListObstaclesFixes());
		fenetre.setObstaclesMobiles(obstaclemanager.getListObstaclesMobiles());		
	}

}
