/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles;

import pfg.kraken.robot.Cinematique;

public class EndWithXY implements EndOfTrajectoryCheck
{

	@Override
	public boolean isArrived(Cinematique endPoint, Cinematique robotPoint)
	{
		return robotPoint.getPosition().squaredDistance(endPoint.getPosition()) < 5;
	}

}
