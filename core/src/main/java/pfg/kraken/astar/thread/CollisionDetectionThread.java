/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.thread;

import pfg.log.Log;
import pfg.kraken.obstacles.container.DynamicObstacles;

/**
 * Thread qui s'occupe de la détection de collisions
 * 
 * @author pf
 *
 */

public class CollisionDetectionThread extends Thread
{
	protected Log log;
	private DynamicObstacles dynObs;
	private DynamicPath pm;
	
	public CollisionDetectionThread(Log log, DynamicObstacles dynObs, DynamicPath pm)
	{
		this.dynObs = dynObs;
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
				/*
				 * On attend que la vérification de collision soit nécessaire
				 */
				synchronized(pm)
				{
					while(!pm.needCollisionCheck())
						pm.wait();
				}
				
				/*
				 * On attend d'avoir des obstacles à vérifier
				 */
				synchronized(dynObs)
				{
					while(!dynObs.needCollisionCheck())
						dynObs.wait();
				}

				pm.updateCollision(dynObs);
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
