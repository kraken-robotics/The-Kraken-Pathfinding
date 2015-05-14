package threads;

import java.io.IOException;
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

public class ThreadSerial extends RobotThread implements Service
{

	protected Log log;
	protected Config config;
	private ObstacleManager obstaclemanager;
	private SerialConnexion serie;
	private Pathfinding pathfinding;
	private Pathfinding strategie;
	
	public ThreadSerial(Log log, Config config, Pathfinding pathfinding, Pathfinding strategie, ObstacleManager obstaclemanager, SerialConnexion serie)
	{
		this.log = log;
		this.config = config;
		this.obstaclemanager = obstaclemanager;
		this.serie = serie;
		this.pathfinding = pathfinding;
		this.strategie = strategie;
		
		Thread.currentThread().setPriority(2);
		updateConfig();
	}

	@Override
	public void run()
	{
		ArrayList<String> data = null;
		while(!stopThreads && !finMatch)
		{
			try {
				serie.wait();
			} catch (InterruptedException e) {
				// TODO
				e.printStackTrace();
			}
			try {
				data = serie.read();
			} catch (IOException e) {
				// TODO
				e.printStackTrace();
			}
			String first = data.get(0);
			switch(first)
			{
				case "obs":
					// TODO: vérifier avant la position brute
					obstaclemanager.creerObstacle(new Vec2<ReadOnly>(Integer.parseInt(data.get(1)), Integer.parseInt(data.get(2))), (int)(System.currentTimeMillis() - Config.getDateDebutMatch()));
					pathfinding.updatePath();
					break;
					
				case "nxt":
					ArrayList<LocomotionNode> itineraire = pathfinding.recomputePath(new GridPoint(Integer.parseInt(data.get(1)), Integer.parseInt(data.get(2))));
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
				
				case "ftl":
					// TODO noter quelque part l'erreur (Table pour strat?)
					strategie.updatePath();
					strategie.recomputePath(null);
					break;

				case "go":
					Config.matchDemarre = true;
					break;
			}
		}
	}
	
	@Override
	public void updateConfig() {
	}

}
