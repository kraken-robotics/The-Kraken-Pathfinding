/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.profiles;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import pfg.kraken.astar.tentacles.TentacleType;
import pfg.kraken.exceptions.UnknownModeException;

/**
 * The manager of the different research profiles
 * @author pf
 *
 */

public class ResearchProfileManager
{
	Map<String, ResearchProfile> profiles = new HashMap<String, ResearchProfile>();

	public int addProfile(ResearchProfile profile)
	{
		// On trie les tâches de la plus coûteuse à la moins coûteuse
		profile.tentacles.sort(Comparator.comparing(TentacleType::getComputationalCost).reversed());
		
		profiles.put(profile.name, profile);
		return profiles.size() - 1;
	}
	
	public ResearchProfile getProfile(String mode) throws UnknownModeException
	{
		ResearchProfile out = profiles.get(mode);
		if(out == null)
			throw new UnknownModeException(mode + " mode is unknown");
		return out;
	}
}
