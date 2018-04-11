/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.thread;

import pfg.log.Log;
import pfg.kraken.obstacles.container.DynamicObstacles;

/**
 * Thread qui s'occupe de la replanification
 * 
 * @author pf
 *
 */

public class AutoReplanningThread extends Thread
{
	protected Log log;
	private DynamicObstacles dynObs;
	private DynamicPath pm;
	private volatile boolean enable = false;
	
	public AutoReplanningThread(Log log, DynamicObstacles dynObs, DynamicPath pm)
	{
		this.dynObs = dynObs;
		this.log = log;
		this.pm = pm;
		setDaemon(true);
		setPriority(Thread.MAX_PRIORITY);
	}
	
	public void setEnable(boolean enable)
	{
		this.enable = enable;
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName(getClass().getSimpleName());
		try
		{
			if(enable)
			{
				synchronized(dynObs)
				{
					dynObs.isThereCollision(pm.getPath());
					dynObs.wait();
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
