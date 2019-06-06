/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.endcheck;

import pfg.kraken.struct.Kinematic;

public class EndWithXY implements EndOfTrajectoryCheck
{

	@Override
	public boolean isArrived(Kinematic endPoint, Kinematic robotPoint)
	{
		return robotPoint.getPosition().squaredDistance(endPoint.getPosition()) < 5;
	}

}
