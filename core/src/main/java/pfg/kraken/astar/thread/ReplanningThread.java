/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.thread;

import pfg.log.Log;
import pfg.kraken.astar.TentacularAStar;
import pfg.kraken.exceptions.PathfindingException;
import pfg.kraken.robot.Cinematique;

/**
 * Thread qui s'occupe de la replanification
 * 
 * @author pf
 *
 */

public final class ReplanningThread extends Thread
{
	protected Log log;
	private TentacularAStar astar;
	private DynamicPath pm;
	
	public ReplanningThread(Log log, TentacularAStar astar, DynamicPath pm)
	{
		this.astar = astar;
		this.log = log;
		this.pm = pm;
		setDaemon(true);
		setPriority(Thread.MAX_PRIORITY);
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName(getClass().getSimpleName());
		try
		{
			while(true)
			{
				try {
					synchronized(pm)
					{
						assert !pm.isStarted();
						while(!pm.isThereSearchRequest() && !pm.isThereACompletePath())
							pm.wait();
					}
					
					/*
					 * Deux possibilités :
					 * - Soit il y a une demande de recherche, auquel cas on cherche
					 * - Soit il y a déjà un chemin, donné manuellement, et on passe la recherche
					 */				

					
					if(pm.isThereSearchRequest())
						//Requête de début de recherche !
						astar.searchWithReplanning();
					else
						assert pm.isThereACompletePath();
					
					while(true)
					{
						Cinematique start;
						synchronized(pm)
						{
							while(!pm.needReplanning() && pm.isStarted())
							{
								pm.checkException();
								pm.wait();
							}
							
							// on vérifie si on doit arrêter la replanification
							pm.checkException();

							// La recherche a été arrêtée
							if(!pm.isStarted())
								break;
														
							assert pm.needReplanning();
/*							if(!pm.needReplanning())
							{
								System.out.println("Pas besoin de replanif");
								break;
							}*/
							start = pm.getNewStart();
						}						
						// sinon, c'est qu'il faut replanifier
						astar.updatePath(start);
					}
				} catch(PathfindingException e)
				{
					/*
					 * On propage l'exception
					 */
					pm.endContinuousSearchWithException(e);
				}				
			}
		}
		catch(InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

}
