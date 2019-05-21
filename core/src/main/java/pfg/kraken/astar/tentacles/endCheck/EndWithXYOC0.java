/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles.endCheck;

import pfg.kraken.robot.Cinematique;
import pfg.kraken.utils.XYO;

public class EndWithXYOC0 implements EndOfTrajectoryCheck
{

	@Override
	public boolean isArrived(Cinematique endPoint, Cinematique robotPoint)
	{
		return robotPoint.getPosition().squaredDistance(endPoint.getPosition()) < 5
				&& Math.abs(XYO.angleDifference(robotPoint.orientationReelle, endPoint.orientationReelle)) < 0.05
				&& Math.abs(robotPoint.courbureReelle) < 0.1;
	}
	
}
