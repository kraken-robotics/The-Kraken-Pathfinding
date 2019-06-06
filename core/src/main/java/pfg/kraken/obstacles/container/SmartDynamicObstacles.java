/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.obstacles.container;

import java.util.ArrayList;
import java.util.List;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.struct.EmbodiedKinematic;

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
	public synchronized int isThereCollision(EmbodiedKinematic[] l, int from, int to)
	{
		for(int i = from; i < to; i++)
		{
			EmbodiedKinematic c = l[i];
			for(Obstacle o : newObs)
				if(o.isColliding(c.obstacle))
				{
					newObs.clear();
					return i;
				}
			i++;
		}
		newObs.clear();
		return to;
	}
	
	protected synchronized void clear()
	{
		newObs.clear();
	}
	
	public synchronized void add(Obstacle obs)
	{
		addObstacle(obs);
		newObs.add(obs);
		notifyAll();
	}

	@Override
	public boolean needCollisionCheck()
	{
		return !newObs.isEmpty();
	}
}
