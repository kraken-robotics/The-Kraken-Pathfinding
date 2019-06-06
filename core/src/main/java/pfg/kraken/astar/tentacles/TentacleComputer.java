/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles;

import pfg.kraken.astar.AStarNode;
import pfg.kraken.struct.Kinematic;

/**
 * An interface to add simply a new tentacle computer
 * @author pf
 *
 */

public interface TentacleComputer
{
	public boolean compute(AStarNode current, TentacleType tentacleType, Kinematic arrival, AStarNode modified, int indexThread);
}
