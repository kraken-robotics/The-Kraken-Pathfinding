/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.obstacles.container;

import java.util.ArrayList;
import java.util.List;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.robot.CinematiqueObs;

/**
 * A default dynamical obstacles manager
 * @author pf
 *
 */

public abstract class SmartDynamicObstacles implements DynamicObstacles
{
	private List<Obstacle> newObs = new ArrayList<Obstacle>();
	
	protected abstract void addObstacle(Obstacle obs);
//	protected abstract void addAllObstacles(Collection<Obstacle> c);
//	protected abstract boolean removeObstacle(Obstacle obs);
//	protected abstract boolean removeAllObstacles(Collection<Obstacle> c);
//	protected abstract void clearObstacles();
	
	@Override
	public synchronized int isThereCollision(List<CinematiqueObs> l)
	{
		int i = 0;
		for(CinematiqueObs c : l)
		{
			for(Obstacle o : newObs)
				if(o.isColliding(c.obstacle))
					return i;
			i++;
		}
		newObs.clear();
		return i;
	}
	
	public synchronized void add(Obstacle obs)
	{
		addObstacle(obs);
		newObs.add(obs);
		notify();
	}

	@Override
	public boolean needCollisionCheck()
	{
		return !newObs.isEmpty();
	}
	
/*	public synchronized void addAll(Collection<Obstacle> c)
	{
		addAllObstacles(c);
		newObs.addAll(c);
		notify();
	}*/
	
/*	public synchronized void remove(Obstacle obs)
	{
		if(removeObstacle(obs))
			notify();
	}*/
	
/*	public synchronized void removeAll(Collection<Obstacle> c)
	{
		if(removeAllObstacles(c))
			notify();
	}

	public synchronized void clear()
	{
		clearObstacles();
		newObs.clear();
		notify();
	}*/
}
