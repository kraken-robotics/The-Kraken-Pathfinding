/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.endcheck;

import pfg.kraken.struct.Kinematic;

/**
 * 
 * @author pf
 *
 */

public interface EndOfTrajectoryCheck
{
	public boolean isArrived(Kinematic endPoint, Kinematic robotPoint);
}
