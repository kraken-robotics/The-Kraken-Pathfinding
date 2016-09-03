package pathfinding;

import serie.BufferOutgoingOrder;
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
	protected Log log;
	private BufferOutgoingOrder out;
	
	private volatile ArcCourbe[] chemin = new ArcCourbe[256];
	private int indexFirst = 0;
	private int indexLast = 0;
	
	public CheminPathfinding(Log log, BufferOutgoingOrder out)
	{
		this.log = log;
		this.out = out;
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
	public synchronized boolean isEmpty()
	{
		return indexFirst == indexLast;
	}

	public synchronized void add(ArcCourbe arc)
	{
		arc.indexTrajectory = indexLast;
		chemin[indexLast++] = arc;
		
		out.envoieArcCourbe(arc);
		
		// si on revient au début, c'est qu'il y a un problème ou que le buffer est sous-dimensionné
		if(indexLast == indexFirst)
			log.critical("Buffer trop petit !");
	}

	public void clear()
	{
		indexLast = indexFirst;
	}

	public synchronized void setCurrentIndex(int indexTrajectory)
	{
		indexFirst = indexTrajectory;
	}

}
