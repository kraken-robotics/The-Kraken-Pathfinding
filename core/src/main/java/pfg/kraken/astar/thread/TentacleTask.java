/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.thread;

import pfg.kraken.astar.AStarNode;
import pfg.kraken.astar.tentacles.TentacleComputer;
import pfg.kraken.astar.tentacles.types.TentacleType;
import pfg.kraken.robot.Cinematique;

/**
 * A structure
 * @author Pierre-François Gimenez
 *
 */

public class TentacleTask {

	public AStarNode current;
	public TentacleType v;
	public Cinematique arrivee;
	public TentacleComputer computer;
	public double vitesseMax;
	
}
