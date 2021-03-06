/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.profiles;

import java.util.List;
import pfg.kraken.astar.endcheck.EndOfTrajectoryCheck;
import pfg.kraken.astar.tentacles.TentacleType;

/**
 * Structure qui contient un profil de recherche
 * @author pf
 *
 */

public class ResearchProfile
{
	public final List<TentacleType> tentacles;
	public final String name;
	public final double coeffDistanceError, coeffDistanceFinalError, coeffAngleError, coeffFinalAngleError, distanceForRealOrientation;
	public final EndOfTrajectoryCheck end;
	
	public ResearchProfile(List<TentacleType> tentacles, String name, double coeffDistanceError, double coeffDistanceFinalError, double coeffAngleError, double coeffFinalAngleError, double distanceForRealOrientation, EndOfTrajectoryCheck end)
	{
		this.tentacles = tentacles;
		this.name = name;
		this.distanceForRealOrientation = distanceForRealOrientation;
		this.coeffDistanceFinalError = coeffDistanceFinalError;
		this.coeffDistanceError = coeffDistanceError;
		this.coeffAngleError = coeffAngleError;
		this.coeffFinalAngleError = coeffFinalAngleError;
		this.end = end;
	}

}
