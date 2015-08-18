package threads;

import container.Service;
import planification.Chemin;
import table.GridSpace;
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
	private GridSpace gridspace;
	private Chemin path;
	
	public ThreadPathfinding(Log log, GridSpace gridspace, Chemin path)
	{
		this.log = log;
		this.gridspace = gridspace;
		this.path = path;
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
			path.updateCost();
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
