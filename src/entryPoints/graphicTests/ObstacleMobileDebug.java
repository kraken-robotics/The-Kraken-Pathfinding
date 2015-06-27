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
			int nbPoints = 1;
			@SuppressWarnings("unchecked")
			Vec2<ReadWrite>[] point = new Vec2[nbPoints];
			Vec2<ReadOnly> positionRobot = new Vec2<ReadOnly>(0,1000);
			int nbCapteurs = 8;
			int[] mesures = new int[nbCapteurs];
/*			mesures[0] = 350;
			mesures[1] = 250;
			mesures[2] = 3000;
			mesures[3] = 3000;
			mesures[4] = 3000;
			mesures[5] = 3000;
			mesures[6] = 3000;
			mesures[7] = 3000;
			
			buffer.add(new IncomingData(positionRobot, 0, 0, mesures));
			Sleep.sleep(100);
			fenetre.repaint();
			if(true)
				return;*/
								
			int dureeSleep = 150;
			for(int k = 0; k < nbPoints; k++)
			{
				point[k] = new Vec2<ReadWrite>(600, 2*Math.PI*k/nbPoints);
				Vec2.plus(point[k], positionRobot);
			}
			
// 			long dateDebut = System.currentTimeMillis();
			for(int i = 0; i < 1000; i++)
			{
//				point.y = i;
				
				for(int k = 0; k < nbPoints; k++)
				{
					point[k].y += (int)(rand.nextGaussian()*10);
					point[k].x += (int)(rand.nextGaussian()*10);
				}
				fenetre.setPoint(point);

				for(int j = 0; j < nbCapteurs; j++)
				{
					mesures[j] = 3000;
					for(int k = 0; k < nbPoints; k++)
					{
						if(capteurs.canBeSeen(point[k].minusNewVector(positionRobot).getReadOnly(), j))
							mesures[j] = Math.min(mesures[j], Math.max((int)(rand.nextGaussian()*bruit) + (int)point[k].distance(positionRobot.plusNewVector(capteurs.positionsRelatives[j]))-200,0));
					}
					log.debug("Capteur "+j+": "+mesures[j]);
				}

				buffer.add(new IncomingData(positionRobot, 0, 0, mesures));
				fenetre.repaint();
				Sleep.sleep(dureeSleep);
			}
//			log.debug("Durée total: "+(System.currentTimeMillis()-dateDebut));
//			log.debug("Fréquence: "+(10000000./(System.currentTimeMillis()-dateDebut)));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
