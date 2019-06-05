/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles.endCheck;

import pfg.kraken.struct.Cinematique;

/**
 * 
 * @author pf
 *
 */

public interface EndOfTrajectoryCheck
{
	public boolean isArrived(Cinematique endPoint, Cinematique robotPoint);
}
