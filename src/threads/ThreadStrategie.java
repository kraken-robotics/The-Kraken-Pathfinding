package threads;

import container.Service;
import strategie.Strategie;
import strategie.StrategieNotifieur;
import utils.Config;
import utils.Log;

/**
 * Thread qui recalcule la stratégie à suivre
 * @author pf
 *
 */

public class ThreadStrategie extends Thread implements Service
{
	protected Log log;
	private StrategieNotifieur notifieur;
	private Strategie strategie;
	
	public ThreadStrategie(Log log, StrategieNotifieur notifieur, Strategie strategie)
	{
		this.log = log;
		this.notifieur = notifieur;
		this.strategie = strategie;
	}

	@Override
	public void run()
	{
		while(true)
		{
			synchronized(notifieur)
			{
				try {
					notifieur.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			// Cet appel peut lancer un pathfinding.notifyAll()
			// Il n'est pas synchronized car il ne modifie rien
			strategie.updateCost();
		}

	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
