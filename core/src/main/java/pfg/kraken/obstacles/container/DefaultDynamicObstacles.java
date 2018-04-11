/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.obstacles.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import pfg.kraken.obstacles.Obstacle;

/**
 * A default dynamical obstacles manager
 * @author pf
 *
 */

public class DefaultDynamicObstacles extends DynamicObstaclesWithoutReplanning
{
	private List<Obstacle> obsList = new ArrayList<Obstacle>();
	
/*	@Override
	public Iterator<Obstacle> getFutureDynamicObstacles(long date)
	{
		return getCurrentDynamicObstacles();
	}*/

	public void add(Obstacle obs)
	{
		obsList.add(obs);
	}

	public void addAll(Collection<Obstacle> obs)
	{
		obsList.addAll(obs);
	}

	public void clear()
	{
		obsList.clear();
	}
	
	@Override
	public Iterator<Obstacle> getCurrentDynamicObstacles()
	{
		return obsList.iterator();
	}

}