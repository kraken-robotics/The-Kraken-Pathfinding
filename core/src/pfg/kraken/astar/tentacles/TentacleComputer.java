/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles;

import pfg.kraken.robot.Cinematique;

/**
 * An interface to add simply a new tentacle computer
 * @author pf
 *
 */

public interface TentacleComputer
{
	public DynamicTentacle compute(Cinematique currentCinem, Tentacle t);
}
