package pathfinding;

import pathfinding.astarCourbe.ArcCourbe;
import utils.Config;
import utils.Log;

import java.util.LinkedList;

import container.Service;

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
	
	private volatile LinkedList<ArcCourbe> chemin = new LinkedList<ArcCourbe>();
	
	public CheminPathfinding(Log log)
	{
		this.log = log;
	}
	
	public void resetChemin()
	{
		chemin.clear();
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

	public synchronized void addArc(ArcCourbe a)
	{
		chemin.add(a);
	}
	
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
		return chemin.poll();
	}

	public boolean isEmpty()
	{
		return chemin.isEmpty();
	}

	public void setFinish(boolean finish)
	{
		this.finish = finish;
	}
	
	public boolean isFinish()
	{
		return finish;
	}

}
