package threads;

import java.util.ArrayList;

import permissions.ReadOnly;
import planification.Pathfinding;
import planification.dstar.GridPoint;
import planification.dstar.LocomotionNode;
import serial.SerialConnexion;
import table.ObstacleManager;
import utils.Config;
import utils.Log;
import utils.Vec2;
import container.Service;
import exceptions.FinMatchException;
import exceptions.SerialConnexionException;

/**
 * Thread qui écoute la série et y répond si besoin.
 * Il peut:
 * - prévenir la table si un obstacle arrive
 * - demander au pathfinding le chemin à suivre
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
	
	public ThreadSerial(Log log, Config config, Pathfinding pathfinding, Pathfinding strategie, ObstacleManager obstaclemanager, SerialConnexion serie, IncomingDataBuffer buffer)
	{
		this.log = log;
		this.config = config;
		this.serie = serie;
		this.pathfinding = pathfinding;
		this.buffer = buffer;
		
		Thread.currentThread().setPriority(2);
		updateConfig();
	}

	@Override
	public void run()
	{
		ThreadLock lock = ThreadLock.getInstance();
		ArrayList<String> data = new ArrayList<String>();
		while(!Config.stopThreads && !Config.finMatch)
		{
			try {
				synchronized(serie)
				{
					serie.wait();
				}
			} catch (InterruptedException e) {
				// TODO
				e.printStackTrace();
			}
			String first = serie.read();
			log.debug(first);
			data.clear();
			switch(first)
			{
				case "obs":
					buffer.add(elem); // TODO
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
				
				case "go":
					Config.matchDemarre = true;
					
					synchronized(lock)
					{
						lock.notifyAll();
					}
					break;
					
			}
		}
	}
	
	@Override
	public void updateConfig() {
		// TODO
	}

}
