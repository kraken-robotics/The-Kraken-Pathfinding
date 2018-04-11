/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.thread;

import pfg.log.Log;
import pfg.kraken.astar.TentacularAStar;
import pfg.kraken.exceptions.PathfindingException;

/**
 * Thread qui s'occupe de la replanification
 * 
 * @author pf
 *
 */

public class ReplanningThread extends Thread
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
						while(!pm.isThereSearchRequest())
							pm.wait();
					}
					
					/*
					 * Requête de début de recherche !
					 */				
					astar.searchWithReplanning();
					
					synchronized(pm)
					{
						while(true)
						{
							while(!pm.needReplanning() && !pm.needToStopReplaning())
								pm.wait();
							// on vérifie si on doit arrêter la replanification
							if(pm.needToStopReplaning())
								break;
							
							// sinon, c'est qu'il faut replanifier
							astar.updatePath(pm.getNewStart());
						}
					}
					
					pm.endSearch();
				} catch(PathfindingException e)
				{
					pm.stopResearch();
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
