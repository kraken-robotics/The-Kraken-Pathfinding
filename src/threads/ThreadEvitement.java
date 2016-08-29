package threads;

import pathfinding.CheminPathfinding;
import pathfinding.astarCourbe.arcs.ArcCourbe;
import serie.BufferOutgoingOrder;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import container.Service;


/**
 * Thread qui gère l'évitement lors d'une trajectoire courbe.
 * Ce n'est pas lui qui calcule le chemin : lui, son rôle, c'est d'envoyer le chemin dans un temps imparti.
 * @author pf
 *
 */

public class ThreadEvitement extends Thread implements Service
{
	private BufferOutgoingOrder serie;
	private ThreadPathfinding threadpathfinding;
	private CheminPathfinding chemin;
	protected Log log;
	
	private int msMaxAvantEvitement;
	
	public ThreadEvitement(Log log, ThreadPathfinding threadpathfinding, BufferOutgoingOrder serie, CheminPathfinding chemin)
	{
		this.log = log;
		this.serie = serie;
		this.threadpathfinding = threadpathfinding;
		this.chemin = chemin;
	}
	
	@Override
	public void run()
	{
		Thread.currentThread().setName("ThreadEvitement");
		while(true)
		{
			try {
				synchronized(threadpathfinding)
				{
					threadpathfinding.wait(); // on attend qu'un nouveau calcul de chemin soit lancé
					synchronized(chemin)
					{
						if(threadpathfinding.isUrgence()) // en cas d'urgence, on attend très peu. sinon, on a plus de temps
							chemin.wait(msMaxAvantEvitement); // grâce au wait, on peut attendre moins si c'est prêt
						else
							chemin.wait(100);
					}
				}			
				synchronized(chemin)
				{
					chemin.demandeCheminPartiel(); // on demande au pathfinding de fixer un chemin partiel
					
					while(chemin.isFinish()) // tant qu'on n'est pas arrivé au bout…
					{
						chemin.wait(); // ça devrait venir vite
						while(!chemin.isEmpty())
						{
							ArcCourbe a = chemin.poll(); // on envoie les chemins un par un
							serie.envoieArcCourbe(a);
						}
					}
				}
			} catch (InterruptedException e2) {
				e2.printStackTrace();
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