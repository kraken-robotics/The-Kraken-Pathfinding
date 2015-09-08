package pathfinding.thetastar;

import utils.Config;
import utils.Log;
import container.Service;

public class CheminPathfinding implements Service
{
	protected Log log;
	private static final int NB_ARCS_MAX = 100;
	private volatile LocomotionArc[] chemin = new LocomotionArc[NB_ARCS_MAX];
	private volatile int dernierIndiceChemin = -1;
	private boolean uptodate;
	private boolean needToStartAgain;
	
	public CheminPathfinding(Log log)
	{
		this.log = log;
		for(int i = 0 ; i < NB_ARCS_MAX; i++)
			chemin[i] = new LocomotionArc();
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

	public synchronized LocomotionArc[] get()
	{
		return chemin;
	}
	
	public synchronized void needToStartAgain()
	{
		needToStartAgain = true;
	}
	
	/**
	 * Demande à ThetaStar s'il faut ou non relancer le pathfinding avec un nouveau départ
	 * @return
	 */
	public synchronized boolean isNeedToStartAgain()
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

}
