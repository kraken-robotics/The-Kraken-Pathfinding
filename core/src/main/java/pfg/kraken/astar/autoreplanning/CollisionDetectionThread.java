/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.autoreplanning;

import pfg.kraken.obstacles.container.DynamicObstacles;

/**
 * Thread qui s'occupe de la détection de collisions
 * 
 * @author pf
 *
 */

public final class CollisionDetectionThread extends Thread
{
	private DynamicObstacles dynObs;
	private DynamicPath pm;
	
	public CollisionDetectionThread(DynamicObstacles dynObs, DynamicPath pm)
	{
		this.dynObs = dynObs;
		this.pm = pm;
		setDaemon(true);
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
