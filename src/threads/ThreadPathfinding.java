package threads;

import obstacles.ObstaclesIterator;
import obstacles.ObstaclesMemory;
import container.Service;
import exceptions.PathfindingException;
import pathfinding.thetastar.ThetaStar;
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
	private ThetaStar pathfinding;
	private ObstaclesMemory obstacles;
	private ObstaclesIterator iterator;
	
	private boolean urgence = false;

	public ThreadPathfinding(Log log, ThetaStar pathfinding, ObstaclesMemory obstacles)
	{
		this.log = log;
		this.pathfinding = pathfinding;
		this.obstacles = obstacles;
		iterator = new ObstaclesIterator(log, obstacles);
	}

	@Override
	public void run()
	{
		while(true)
		{
			synchronized(obstacles)
			{
				try {
					obstacles.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// TODO : il faut qu'il ne fasse rien si on n'est pas en déplacement entre scripts
			iterator.reinitNow();
			while(iterator.hasNext())
				if(iterator.next().isUrgent())
				{
					urgence = true;
					break;
				}
//			try {
//				pathfinding.updatePath();
//			} catch (PathfindingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}

//		log.debug("Fermeture de ThreadPathfinding");
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

	public final boolean isUrgence()
	{
		return urgence;
	}
	
}
