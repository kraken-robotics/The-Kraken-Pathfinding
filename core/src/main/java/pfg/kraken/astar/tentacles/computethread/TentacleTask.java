/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles.computethread;

import pfg.kraken.astar.AStarNode;
import pfg.kraken.astar.tentacles.TentacleComputer;
import pfg.kraken.astar.tentacles.TentacleType;
import pfg.kraken.struct.Kinematic;
import pfg.kraken.struct.XY;

/**
 * A structure
 * @author Pierre-François Gimenez
 *
 */

public class TentacleTask {

	public volatile AStarNode current;
	public volatile TentacleType v;
	public volatile Kinematic arrivee;
	public volatile TentacleComputer computer;
	public volatile double vitesseMax;
	public volatile XY startPosition;
}
