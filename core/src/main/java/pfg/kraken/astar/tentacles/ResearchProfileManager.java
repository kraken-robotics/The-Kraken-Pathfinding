/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles;

import java.util.ArrayList;
import java.util.List;
import pfg.kraken.astar.tentacles.types.TentacleType;

/**
 * The manager of the different research profiles
 * @author pf
 *
 */

public class ResearchProfileManager
{
	List<List<TentacleType>> profiles = new ArrayList<List<TentacleType>>();
	
	public int addProfile(List<TentacleType> p)
	{
		profiles.add(p);
		return profiles.size() - 1;
	}
	
	public List<TentacleType> getProfile(int index)
	{
		return profiles.get(index);
	}
}
