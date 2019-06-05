/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles.computethread;

import pfg.kraken.astar.AStarNode;
import pfg.kraken.astar.tentacles.TentacleComputer;
import pfg.kraken.astar.tentacles.types.TentacleType;
import pfg.kraken.struct.Cinematique;

/**
 * A structure
 * @author Pierre-François Gimenez
 *
 */

public class TentacleTask {

	public volatile AStarNode current;
	public volatile TentacleType v;
	public volatile Cinematique arrivee;
	public volatile TentacleComputer computer;
	public volatile double vitesseMax;
}
