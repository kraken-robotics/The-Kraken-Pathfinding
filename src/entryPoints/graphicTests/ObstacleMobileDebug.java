package entryPoints.graphicTests;

import buffer.IncomingData;
import buffer.IncomingDataBuffer;
import permissions.ReadOnly;
import permissions.ReadWrite;
import table.Capteurs;
import table.ObstacleManager;
import tests.graphicLib.Fenetre;
import utils.Log;
import utils.Sleep;
import utils.Vec2;
import container.Container;
import container.ServiceNames;

/**
 * Test grahique d'obstacles
 * @author pf
 *
 */

public class ObstacleMobileDebug  {

	public static void main(String[] args)
	{
		try {
			Container container = new Container();
			Log log = (Log) container.getService(ServiceNames.LOG);
			ObstacleManager obstaclemanager = (ObstacleManager) container.getService(ServiceNames.OBSTACLE_MANAGER);
			IncomingDataBuffer buffer = (IncomingDataBuffer) container.getService(ServiceNames.INCOMING_DATA_BUFFER);
			Fenetre fenetre = new Fenetre();
			fenetre.setObstaclesMobiles(obstaclemanager.getListObstaclesMobiles());
			fenetre.showOnFrame();
			Vec2<ReadWrite> point = new Vec2<ReadWrite>(500, 0);
			Vec2<ReadOnly> positionRobot = new Vec2<ReadOnly>(0,1000);
			int[] mesures = new int[2];
			for(int i = 500; i < 1500; i+=10)
			{
				point.y = i;
				fenetre.setPoint(point.getReadOnly());
				if(Capteurs.canBeSeen(point.minusNewVector(positionRobot).getReadOnly(), 0))
					mesures[0] = (int)point.distance(positionRobot.plusNewVector(Capteurs.positionsRelatives[0]))-200;
				else
				{
					log.debug("Capteur du haut ne voit rien");
					mesures[0] = 0;
				}
				
				if(Capteurs.canBeSeen(point.minusNewVector(positionRobot).getReadOnly(), 1))
					mesures[1] = (int)point.distance(positionRobot.plusNewVector(Capteurs.positionsRelatives[1]))-200;
				else
				{
					log.debug("Capteur du bas ne voit rien");
					mesures[1] = 0;
				}
				buffer.add(new IncomingData(positionRobot, 0, 0, mesures));
				fenetre.updateFirstNotDead(obstaclemanager.getFirstNotDead());
				fenetre.repaint();
				Sleep.sleep(200);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
