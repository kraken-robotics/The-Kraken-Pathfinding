package pathfinding.thetastar;

import pathfinding.astar_courbe.ArcCourbe;
import utils.Config;
import utils.Log;
import container.Service;

public class CheminPathfinding implements Service
{
	protected Log log;
	private static final int NB_ARCS_MAX = 100;
	private volatile ArcCourbe[] chemin = new ArcCourbe[NB_ARCS_MAX];
	private volatile int dernierIndiceChemin = -1;
	private volatile boolean uptodate;
	private volatile boolean needToStartAgain;
	private volatile boolean last;
	
	public CheminPathfinding(Log log)
	{
		this.log = log;
		for(int i = 0 ; i < NB_ARCS_MAX; i++)
			chemin[i] = new ArcCourbe();
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

	public synchronized ArcCourbe[] get()
	{
		return chemin;
	}
	
	/**
	 * Demande à ThetaStar s'il faut ou non relancer le pathfinding avec un nouveau départ
	 * @return
	 */
	public synchronized boolean isNeededToStartAgain()
	{
		boolean out = needToStartAgain;
		needToStartAgain = false;
		return out;
	}

	public synchronized void setDernierIndiceChemin(int dernierIndiceChemin)
	{
		uptodate = true;
		this.dernierIndiceChemin = dernierIndiceChemin;
		notify();
	}

	public int getDernierIndiceChemin()
	{
		needToStartAgain = true;
		return dernierIndiceChemin;
	}
	
	public boolean isUptodate()
	{
		return uptodate;
	}
	
	public void notUptodate()
	{
		uptodate = false;
	}

	public void setLast(boolean last)
	{
		this.last = last;
	}
	
	public boolean isLast()
	{
		return last;
	}

}
