/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.obstacles.container;

import pfg.kraken.struct.EmbodiedKinematic;

/**
 * An dynamical obstacles that works without replanning
 * @author pf
 *
 */

public abstract class DynamicObstaclesWithoutReplanning implements DynamicObstacles
{
	@Override
	public int isThereCollision(EmbodiedKinematic[] l, int from, int to)
	{
		return to;
	}

}
