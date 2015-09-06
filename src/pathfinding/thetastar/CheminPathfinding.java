package pathfinding.thetastar;

import java.util.LinkedList;

import utils.Config;
import utils.Log;
import container.Service;

public class CheminPathfinding implements Service
{
	protected Log log;
	private volatile LinkedList<LocomotionArc> chemin = new LinkedList<LocomotionArc>();

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

	public synchronized void set(LinkedList<LocomotionArc> cheminTmp)
	{
		chemin = cheminTmp;
	}

	public synchronized LinkedList<LocomotionArc> get()
	{
		return chemin;
	}
	
	/**
	 * Signale à ThetaStar s'il faut ou non relancer le pathfinding avec un nouveau départ
	 * @return
	 */
	public synchronized boolean needToStartAgain()
	{
		return false;
	}

}
