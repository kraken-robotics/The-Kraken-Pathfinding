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
	protected Log log;
	private volatile boolean doitFixerCheminPartiel;
	private volatile boolean finish;
	
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
	
	/**
	 * Le pathfinding demande s'il faut fixer le chemin partiel
	 * @return
	 */
	public synchronized boolean doitFixerCheminPartiel()
	{
		boolean out = doitFixerCheminPartiel;
		doitFixerCheminPartiel = false;
		return out;
	}

	public synchronized void demandeCheminPartiel()
	{
		doitFixerCheminPartiel = true;
	}

	public ArcCourbe poll()
	{
		return chemin[indexFirst++];
	}

	public boolean isEmpty()
	{
		return indexFirst == indexLast;
	}

	public void setFinish(boolean finish)
	{
		this.finish = finish;
	}
	
	public boolean isFinish()
	{
		return finish;
	}

	public void add(ArcCourbe arc)
	{
		arc.indexTrajectory = indexLast;
		chemin[indexLast++] = arc;
		
		// si on revient au début, c'est qu'il y a un problème ou que le buffer est sous-dimensionné
		if(indexLast == indexFirst)
			log.critical("Buffer trop petit !");
	}

	public void clear()
	{
		indexLast = indexFirst;
	}

	public void setCurrentIndex(int indexTrajectory)
	{
		indexFirst = indexTrajectory;
	}

}
