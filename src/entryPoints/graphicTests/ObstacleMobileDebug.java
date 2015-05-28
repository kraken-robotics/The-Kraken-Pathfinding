package entryPoints.graphicTests;

import table.ObstacleManager;
import tests.graphicLib.Fenetre;
import utils.Sleep;
import container.Container;
import container.ServiceNames;

/**
 * Tests unitaires disposant d'une interface graphique.
 * Utilisé pour la vérification humaine.
 * @author pf
 *
 */

public class ObstacleMobileDebug  {

	public static void main(String[] args)
	{
		try {
			Container container = new Container();
			ObstacleManager obstaclemanager = (ObstacleManager) container.getService(ServiceNames.OBSTACLE_MANAGER);
			Fenetre fenetre = new Fenetre();
			fenetre.setObstaclesMobiles(obstaclemanager.getListObstaclesMobiles());
			fenetre.showOnFrame();
			while(true)
			{
				fenetre.updateFirstNotDead(obstaclemanager.getFirstNotDead());
				fenetre.repaint();
				Sleep.sleep(100);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
