/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pfg.kraken.astar.tentacles.types.TentacleType;
import pfg.kraken.exceptions.UnknownModeException;

/**
 * The manager of the different research profiles
 * @author pf
 *
 */

public class ResearchProfileManager// implements Iterable<List<TentacleType>>
{
	Map<String, List<TentacleType>> profiles = new HashMap<String, List<TentacleType>>();
	Map<String, EndOfTrajectoryCheck> ends = new HashMap<String, EndOfTrajectoryCheck>();

	public int addProfile(String mode, List<TentacleType> p, EndOfTrajectoryCheck end)
	{
		// On trie les tâches de la plus coûteuse à la moins coûteuse
		p.sort(Comparator.comparing(TentacleType::getComputationalCost).reversed());
		
		profiles.put(mode, p);
		ends.put(mode, end);
		return profiles.size() - 1;
	}
	
	public List<TentacleType> getProfile(String mode) throws UnknownModeException
	{
		List<TentacleType> out = profiles.get(mode);
		if(out == null)
			throw new UnknownModeException(mode + " mode is unknown");
		return out;
	}
	
	public EndOfTrajectoryCheck getEndCheck(String mode) throws UnknownModeException
	{
		EndOfTrajectoryCheck out = ends.get(mode);
		if(out == null)
			throw new UnknownModeException(mode + " mode is unknown");
		return out;
	}

/*	@Override
	public Iterator<List<TentacleType>> iterator()
	{
		return profiles.iterator();
	}*/
}
