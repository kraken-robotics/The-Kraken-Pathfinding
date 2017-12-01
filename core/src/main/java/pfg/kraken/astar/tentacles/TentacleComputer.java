/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles;

import pfg.kraken.astar.AStarNode;
import pfg.kraken.astar.tentacles.types.TentacleType;
import pfg.kraken.robot.Cinematique;

/**
 * An interface to add simply a new tentacle computer
 * @author pf
 *
 */

public interface TentacleComputer
{
	public boolean compute(AStarNode current, TentacleType tentacleType, Cinematique arrival, AStarNode modified, int indexThread);
}
