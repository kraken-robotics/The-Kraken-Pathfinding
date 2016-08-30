package pathfinding;

import utils.Config;
import utils.Log;

import container.Service;
import pathfinding.astarCourbe.arcs.ArcCourbe;

/**
 * S'occupe de la trajectoire actuelle.
 * Notifie dès qu'un chemin (partiel ou complet) est disponible
 * @author pf
 *
 */

public class CheminPathfinding implements Service
{
	public Object mutexRead = new Object(), mutexWrite = new Object();
	protected Log log;
	
	private volatile ArcCourbe[] chemin = new ArcCourbe[256];
	private int indexFirst = 0;
	private int indexLast = 0;
	
	public CheminPathfinding(Log log)
	{
		this.log = log;
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
	public ArcCourbe poll()
	{
		return chemin[indexFirst++];
	}

	public boolean isEmpty()
	{
		synchronized(mutexWrite)
		{
			return indexFirst == indexLast;
		}
	}

	public void add(ArcCourbe arc)
	{
		synchronized(mutexWrite)
		{
			arc.indexTrajectory = indexLast;
			chemin[indexLast++] = arc;
			
			// si on revient au début, c'est qu'il y a un problème ou que le buffer est sous-dimensionné
			if(indexLast == indexFirst)
				log.critical("Buffer trop petit !");
		}
	}

	public void clear()
	{
		synchronized(mutexRead)
		{
			indexLast = indexFirst;
		}
	}

	public void setCurrentIndex(int indexTrajectory)
	{
		synchronized(mutexWrite)
		{
			indexFirst = indexTrajectory;
		}
	}

}
