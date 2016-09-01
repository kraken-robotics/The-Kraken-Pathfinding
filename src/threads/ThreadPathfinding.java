package threads;

import container.Service;
import pathfinding.astarCourbe.AStarCourbe;
import utils.Config;
import utils.Log;

/**
 * Thread qui recalcule l'itinéraire à emprunter. Surveille ObstaclesMemory.
 * @author pf
 *
 */

public class ThreadPathfinding extends Thread implements Service
{
	protected Log log;
	private AStarCourbe pathfinding;

	public ThreadPathfinding(Log log, AStarCourbe pathfinding)
	{
		this.log = log;
		this.pathfinding = pathfinding;
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName("ThreadPathfinding");
		log.debug("Démarrage de "+Thread.currentThread().getName());
		try {
			while(true)
				pathfinding.doYourJob();
		} catch (InterruptedException e) {
			log.debug("Arrêt de "+Thread.currentThread().getName());
		}
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
}
