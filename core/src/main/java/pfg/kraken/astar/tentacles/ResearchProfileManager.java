/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pfg.config.Config;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.astar.tentacles.types.TentacleType;
import pfg.kraken.exceptions.UnknownModeException;

/**
 * The manager of the different research profiles
 * @author pf
 *
 */

public class ResearchProfileManager// implements Iterable<List<TentacleType>>
{
	Map<String, List<List<TentacleType>>> profiles = new HashMap<String, List<List<TentacleType>>>();
	Map<String, EndOfTrajectoryCheck> ends = new HashMap<String, EndOfTrajectoryCheck>();
	
	private int nbThread;
	
	public ResearchProfileManager(Config config)
	{
		this.nbThread = config.getInt(ConfigInfoKraken.THREAD_NUMBER);
	}
	
	public int addProfile(String mode, List<TentacleType> p, EndOfTrajectoryCheck end)
	{
		List<List<TentacleType>> distribution = new ArrayList<List<TentacleType>>();

		for(int i = 0; i < nbThread; i++)
			distribution.add(new ArrayList<TentacleType>());
		
		// TODO : problem "Multiprocessor scheduling"
		int index = 0;
		for(TentacleType t : p)
		{
			distribution.get(index).add(t);
			index++;
			index %= nbThread;
		}
		
		profiles.put(mode, distribution);
		ends.put(mode, end);
		return profiles.size() - 1;
	}
	
	public List<List<TentacleType>> getProfile(String mode) throws UnknownModeException
	{
		List<List<TentacleType>> out = profiles.get(mode);
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
