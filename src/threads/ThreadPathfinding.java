package threads;

import container.Service;
import exceptions.PathfindingException;
import pathfinding.astarCourbe.AStarCourbe;
import pathfinding.dstarlite.GridSpace;
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
	private GridSpace gridspace;

	public ThreadPathfinding(Log log, AStarCourbe pathfinding, GridSpace gridspace)
	{
		this.log = log;
		this.pathfinding = pathfinding;
		this.gridspace = gridspace;
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName("ThreadPathfinding");
		while(true)
		{
			/**
			 * En attente d'une modification des obstacles
			 */
			synchronized(gridspace)
			{
				try {
					gridspace.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			log.debug("Recalcul de trajectoire !");
			try {
				synchronized(this)
				{
					notify(); // on prévient le thread d'évitement qu'un nouveau chemin est en calcul
					pathfinding.updatePath();
				}
			} catch (PathfindingException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
}
