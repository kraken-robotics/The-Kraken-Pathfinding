package threads;

import java.util.BitSet;

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
	private boolean urgence = false;

	public ThreadPathfinding(Log log, AStarCourbe pathfinding, GridSpace gridspace)
	{
		this.log = log;
		this.pathfinding = pathfinding;
		this.gridspace = gridspace;
	}

	@Override
	public void run()
	{
		if(true) return; // Adieu
		while(true)
		{
			synchronized(gridspace)
			{
				try {
					gridspace.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			log.debug("Recalcule de trajectoire !");
/*			try {
				synchronized(this)
				{
					notify(); // on prévient le thread d'évitement qu'un nouveau chemin est en calcul
					// TODO thread pathfinding qui communique avec AStarCourbe
					pathfinding.updatePath();
				}
			} catch (PathfindingException e) {
				e.printStackTrace();
			}*/
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
