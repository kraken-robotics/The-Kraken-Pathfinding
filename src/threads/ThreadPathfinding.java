package threads;

import container.Service;
import pathfinding.DStarLite;
import pathfinding.GridSpace;
import utils.Config;
import utils.Log;

/**
 * Thread qui recalcule l'itinéraire à emprunter. Surveille GridSpace.
 * @author pf
 *
 */

public class ThreadPathfinding extends Thread implements Service
{
	protected Log log;
	private DStarLite pathfinding;
	private GridSpace gridspace;
	
	public ThreadPathfinding(Log log, DStarLite pathfinding, GridSpace gridspace)
	{
		this.log = log;
		this.pathfinding = pathfinding;
		this.gridspace = gridspace;
	}

	@Override
	public void run()
	{
		while(true)
		{
			synchronized(gridspace)
			{
				try {
					gridspace.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			log.debug("Réveil de ThreadPathfinding");	
			
			// Cet appel peut lancer un pathfinding.notifyAll()
			// Il n'est pas synchronized car il ne modifie pas obstaclemanager
			pathfinding.updatePath();
		}

//		log.debug("Fermeture de ThreadPathfinding");
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
}
