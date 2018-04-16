/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles;

import java.util.List;

import pfg.kraken.astar.tentacles.endCheck.EndOfTrajectoryCheck;
import pfg.kraken.astar.tentacles.types.TentacleType;

/**
 * Structure qui contient un profil de recherche
 * @author pf
 *
 */

public class ResearchProfile
{
	public final List<TentacleType> tentacles;
	public final String name;
	public final double coeffDistanceError, coeffAngleError, coeffFinalAngleError;
	public final EndOfTrajectoryCheck end;
	
	public ResearchProfile(List<TentacleType> tentacles, String name, double coeffDistanceError, double coeffAngleError, double coeffFinalAngleError, EndOfTrajectoryCheck end)
	{
		this.tentacles = tentacles;
		this.name = name;
		this.coeffDistanceError = coeffDistanceError;
		this.coeffAngleError = coeffAngleError;
		this.coeffFinalAngleError = coeffFinalAngleError;
		this.end = end;
	}

}
