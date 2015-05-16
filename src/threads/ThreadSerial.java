package threads;

import java.util.ArrayList;

import permissions.ReadOnly;
import planification.Pathfinding;
import planification.dstar.GridPoint;
import planification.dstar.LocomotionNode;
import requete.RequeteSTM;
import requete.RequeteType;
import serial.SerialConnexion;
import table.ObstacleManager;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2;
import container.Service;
import enums.RobotColor;
import exceptions.FinMatchException;
import exceptions.SerialConnexionException;

/**
 * Thread qui écoute la série et appelle qui il faut.
 * @author pf
 *
 */

public class ThreadSerial extends Thread implements Service
{

	protected Log log;
	protected Config config;
	private SerialConnexion serie;
	private Pathfinding pathfinding;
	private IncomingDataBuffer buffer;
	private RequeteSTM requete;
	
	public ThreadSerial(Log log, Config config, Pathfinding pathfinding, Pathfinding strategie, ObstacleManager obstaclemanager, SerialConnexion serie, IncomingDataBuffer buffer)
	{
		this.log = log;
		this.config = config;
		this.serie = serie;
		this.pathfinding = pathfinding;
		this.buffer = buffer;
		requete = RequeteSTM.getInstance();
		
		Thread.currentThread().setPriority(2);
		updateConfig();
	}

	@Override
	public void run()
	{
		/**
		 * StartMatchLock permet de signaler le départ du match aux autres threads
		 * Il est utilisé par ThreadTimer
		 */
		StartMatchLock lock = StartMatchLock.getInstance();
		ArrayList<String> data = new ArrayList<String>();
		while(!Config.stopThreads && !Config.finMatch)
		{
			try {
				synchronized(serie)
				{
					serie.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}
			String first = serie.read();
			log.debug(first);
			data.clear();
			switch(first)
			{
				case "obs":
					int xBrut = Integer.parseInt(serie.read());
					int yBrut = Integer.parseInt(serie.read());
					int xEnnemi = Integer.parseInt(serie.read());
					int yEnnemi = Integer.parseInt(serie.read());

					buffer.add(new IncomingData(new Vec2<ReadOnly>(xBrut, yBrut), new Vec2<ReadOnly>(xEnnemi, yEnnemi)));
					break;

				case "nxt":
					int x = Integer.parseInt(serie.read());
					int y = Integer.parseInt(serie.read());
					// Réécrire avec x, y
					ArrayList<LocomotionNode> itineraire = pathfinding.getPath(new GridPoint(x,y));
					try {
						serie.communiquer(itineraire);
					} catch (SerialConnexionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (FinMatchException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				
				case "enm":
					/**
					 * On signale au thread principal qu'il y a un problème.
					 * C'est lui qui répondre à la STM.
					 */
					synchronized(requete)
					{
						requete.type = RequeteType.OBSTACLE_DROIT_DEVANT;
						requete.notifyAll();
					}
					break;
					
				case "clr":
					config.set(ConfigInfo.COULEUR, RobotColor.parse(serie.read()));
					break;
					
				case "go":
					/**
					 * Démarrage du match
					 */
					synchronized(lock)
					{
						lock.notifyAll();
					}
					break;
					
				case "end":
					/**
					 * Fin du match, on coupe la série et on arrête ce thread
					 */
					serie.close();
					return;

				case "arv":
					synchronized(requete)
					{
						requete.type = RequeteType.TRAJET_FINI;
						requete.notifyAll();
					}
					break;
					
			}
		}
	}
	
	@Override
	public void updateConfig()
	{}

}
