package threads;

import pathfinding.CheminPathfinding;
import pathfinding.astarCourbe.arcs.ArcCourbe;
import serie.BufferOutgoingOrder;
import utils.Config;
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
	private CheminPathfinding chemin;
	protected Log log;
	
	public ThreadEvitement(Log log, BufferOutgoingOrder serie, CheminPathfinding chemin)
	{
		this.log = log;
		this.serie = serie;
		this.chemin = chemin;
	}
	
	@Override
	public void run()
	{
		Thread.currentThread().setName("ThreadEvitement");
		log.debug("Démarrage de "+Thread.currentThread().getName());
		try {
			while(true)
			{
				synchronized(chemin)
				{
					if(chemin.isEmpty())
						chemin.wait();
					while(!chemin.isEmpty())
					{
						ArcCourbe a = chemin.poll(); // on envoie les chemins un par un
						serie.envoieArcCourbe(a);
					}
				}			
			}
		} catch (InterruptedException e2) {
			log.debug("Arrêt de "+Thread.currentThread().getName());
		}
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}