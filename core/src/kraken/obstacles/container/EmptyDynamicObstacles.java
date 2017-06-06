/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package kraken.obstacles.container;

import java.util.Collections;
import java.util.Iterator;

import kraken.obstacles.types.ObstacleMasque;

/**
 * An empty dynamical obstacles manager
 * @author pf
 *
 */

public class EmptyDynamicObstacles implements DynamicObstacles
{
	@Override
	public Iterator<ObstacleMasque> getFutureDynamicObstacles(long date)
	{
		return getCurrentDynamicObstacles();
	}

	@Override
	public Iterator<ObstacleMasque> getCurrentDynamicObstacles()
	{
		return Collections.emptyIterator();
	}

}
