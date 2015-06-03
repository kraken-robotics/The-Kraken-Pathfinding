package entryPoints.graphicTests;

import java.util.Random;

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
 * Test graphique d'obstacles
 * @author pf
 *
 */

public class ObstacleMobileDebug  {

	public static void main(String[] args)
	{
		try {
			Random rand = new Random();
			int bruit = 10;
			Container container = new Container();
			Log log = (Log) container.getService(ServiceNames.LOG);
			Capteurs capteurs = (Capteurs) container.getService(ServiceNames.CAPTEURS);
			ObstacleManager obstaclemanager = (ObstacleManager) container.getService(ServiceNames.OBSTACLE_MANAGER);
			IncomingDataBuffer buffer = (IncomingDataBuffer) container.getService(ServiceNames.INCOMING_DATA_BUFFER);
			Fenetre fenetre = new Fenetre();
			fenetre.setCapteurs(capteurs);
			fenetre.setObstaclesMobiles(obstaclemanager.getListObstaclesMobiles());
			fenetre.showOnFrame();
			Vec2<ReadWrite> point = new Vec2<ReadWrite>(500, 0);
			Vec2<ReadOnly> positionRobot = new Vec2<ReadOnly>(0,1000);
			int[] mesures = new int[2];
			/*mesures[0] = 253;
			mesures[1] = 319;
			
			buffer.add(new IncomingData(positionRobot, 0, 0, mesures));
			Sleep.sleep(100);
			fenetre.repaint();
			if(true)
				return;*/
				
			for(int i = 500; i < 1500; i+=10)
			{
				point.y = i;
				fenetre.setPoint(point.getReadOnly());
				if(capteurs.canBeSeen(point.minusNewVector(positionRobot).getReadOnly(), 0))
				{
					mesures[0] = (int)(rand.nextGaussian()*bruit) + (int)point.distance(positionRobot.plusNewVector(capteurs.positionsRelatives[0]))-200;
//					log.debug("Capteur du haut voit: "+mesures[0]);
				}
				else
				{
					mesures[0] = 0;
				}
				
				if(capteurs.canBeSeen(point.minusNewVector(positionRobot).getReadOnly(), 1))
				{
					mesures[1] = (int)(rand.nextGaussian()*bruit) + (int)point.distance(positionRobot.plusNewVector(capteurs.positionsRelatives[1]))-200;
//					log.debug("Capteur du bas voit: "+mesures[1]);
				}
				else
				{
					mesures[1] = 0;
				}
				buffer.add(new IncomingData(positionRobot, 0, 0, mesures));
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
