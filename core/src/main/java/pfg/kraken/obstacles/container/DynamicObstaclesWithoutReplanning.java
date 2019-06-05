/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.obstacles.container;

import pfg.kraken.struct.CinematiqueObs;

/**
 * An dynamical obstacles that works without replanning
 * @author pf
 *
 */

public abstract class DynamicObstaclesWithoutReplanning implements DynamicObstacles
{
	@Override
	public int isThereCollision(CinematiqueObs[] l, int from, int to)
	{
		return to;
	}

}
