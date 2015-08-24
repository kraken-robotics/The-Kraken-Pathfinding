package pathfinding.thetastar;

import java.util.ArrayList;

import utils.Config;
import container.Service;

public class CheminPathfinding implements Service
{
	private volatile ArrayList<LocomotionArc> chemin = new ArrayList<LocomotionArc>();

	@Override
	public void updateConfig(Config config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void useConfig(Config config) {
		// TODO Auto-generated method stub
		
	}

	public synchronized void set(ArrayList<LocomotionArc> cheminTmp)
	{
		// TODO Auto-generated method stub
		
	}

	public synchronized void get(ArrayList<LocomotionArc> cheminTmp)
	{
		// TODO Auto-generated method stub
		
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
