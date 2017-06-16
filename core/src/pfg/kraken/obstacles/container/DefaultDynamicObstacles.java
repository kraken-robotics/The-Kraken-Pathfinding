/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.obstacles.container;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pfg.kraken.obstacles.types.Obstacle;
import pfg.kraken.obstacles.types.ObstacleMasque;

/**
 * A default dynamical obstacles manager
 * @author pf
 *
 */

public class DefaultDynamicObstacles implements DynamicObstacles
{
	private List<ObstacleMasque> obsList = new ArrayList<ObstacleMasque>();
	
	@Override
	public Iterator<ObstacleMasque> getFutureDynamicObstacles(long date)
	{
		return getCurrentDynamicObstacles();
	}

	public void add(Obstacle obs)
	{
		obsList.add(new ObstacleMasque(obs));
	}
	
	public void clear()
	{
		obsList.clear();
	}
	
	@Override
	public Iterator<ObstacleMasque> getCurrentDynamicObstacles()
	{
		return obsList.iterator();
	}

}
