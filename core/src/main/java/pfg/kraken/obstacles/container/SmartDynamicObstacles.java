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
	protected List<Obstacle> newObs = new ArrayList<Obstacle>();
	
	protected abstract void addObstacle(Obstacle obs);

	@Override
	public synchronized int isThereCollision(List<CinematiqueObs> l)
	{
		int i = 0;
		for(CinematiqueObs c : l)
		{
			for(Obstacle o : newObs)
				if(o.isColliding(c.obstacle))
				{
					newObs.clear();
					return i;
				}
			i++;
		}
		newObs.clear();
		return i;
	}
	
	protected synchronized void clear()
	{
		newObs.clear();
	}
	
	public synchronized void add(Obstacle obs)
	{
		System.out.println("Ajout d'un obstacle : "+obs);

		addObstacle(obs);
		newObs.add(obs);
		notify();
	}

	@Override
	public boolean needCollisionCheck()
	{
		return !newObs.isEmpty();
	}
}
