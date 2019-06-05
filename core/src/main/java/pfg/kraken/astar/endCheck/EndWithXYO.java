/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.endCheck;

import pfg.kraken.struct.Cinematique;
import pfg.kraken.struct.XYO;

public class EndWithXYO implements EndOfTrajectoryCheck
{

	@Override
	public boolean isArrived(Cinematique endPoint, Cinematique robotPoint)
	{
		return robotPoint.getPosition().squaredDistance(endPoint.getPosition()) < 5
				&& Math.abs(XYO.angleDifference(robotPoint.orientationReelle, endPoint.orientationReelle)) < 0.05;
	}

}
