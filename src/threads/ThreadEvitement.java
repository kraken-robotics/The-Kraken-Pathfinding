package threads;

import pathfinding.thetastar.CheminPathfinding;
import pathfinding.thetastar.LocomotionArc;
import pathfinding.thetastar.ThetaStar;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import buffer.DataForSerialOutput;
import container.Service;


/**
 * Thread qui gère l'évitement lors d'une trajectoire courbe
 * @author pf
 *
 */

public class ThreadEvitement extends Thread implements Service
{
	private ThetaStar pathfinding;
	private DataForSerialOutput serie;
	private ThreadPathfinding threadpathfinding;
	private CheminPathfinding chemin;
	protected Log log;
	
	private int msMaxAvantEvitement;
	
	public ThreadEvitement(Log log, ThreadPathfinding threadpathfinding, DataForSerialOutput serie, ThetaStar pathfinding, CheminPathfinding chemin)
	{
		this.log = log;
		this.pathfinding = pathfinding;
		this.serie = serie;
		this.threadpathfinding = threadpathfinding;
		this.chemin = chemin;
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			try {
				synchronized(threadpathfinding)
				{
					threadpathfinding.wait();
					synchronized(pathfinding)
					{
						if(threadpathfinding.isUrgence())
							pathfinding.wait(msMaxAvantEvitement);
						else
							pathfinding.wait(100);
					}
				}			
			} catch (InterruptedException e2) {
				e2.printStackTrace();
			}
			try {
				synchronized(chemin)
				{
					while(!chemin.isUptodate())
							chemin.wait();
					LocomotionArc[] arcs = chemin.get();
					int dernierIndice = chemin.getDernierIndiceChemin();

					serie.envoieLocomotionArcFirst(arcs[dernierIndice]);
					for(int i = dernierIndice - 1 ; i >= 0 ; i--)
						serie.envoieLocomotionArc(arcs[i]);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		msMaxAvantEvitement = config.getInt(ConfigInfo.MS_MAX_AVANT_EVITEMENT);
	}

}