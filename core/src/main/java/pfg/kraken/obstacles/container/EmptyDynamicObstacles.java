/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.obstacles.container;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.robot.CinematiqueObs;

/**
 * An empty dynamical obstacles manager
 * @author pf
 *
 */

public class EmptyDynamicObstacles implements DynamicObstacles
{
	@Override
	public int isThereCollision(List<CinematiqueObs> l)
	{
		return l.size();
	}

	@Override
	public Iterator<Obstacle> getCurrentDynamicObstacles()
	{
		return Collections.emptyIterator();
	}

	@Override
	public boolean needCollisionCheck()
	{
		return false;
	}

}
