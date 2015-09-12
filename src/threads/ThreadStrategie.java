package threads;

import container.Service;
import strategie.StrategieNotifieur;
import strategie.lpastar.LPAStar;
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
	private LPAStar strategie;
	
	public ThreadStrategie(Log log, StrategieNotifieur notifieur, LPAStar strategie)
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
//			strategie.updateCost();
		}
//		log.debug("Fermeture de ThreadStrategie");

	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
