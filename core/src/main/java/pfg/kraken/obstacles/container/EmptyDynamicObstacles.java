/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.obstacles.container;

import java.util.Collections;
import java.util.Iterator;

import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.struct.EmbodiedKinematic;

/**
 * An empty dynamical obstacles manager
 * @author pf
 *
 */

public final class EmptyDynamicObstacles implements DynamicObstacles
{
	@Override
	public int isThereCollision(EmbodiedKinematic[] l, int from, int to)
	{
		return to;
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
