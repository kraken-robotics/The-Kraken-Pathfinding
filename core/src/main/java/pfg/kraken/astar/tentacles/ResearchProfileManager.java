/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles;

import java.util.ArrayList;
import java.util.Comparator;
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
	
	/**
	 * Un thread et sa charge théorique
	 * @author pf
	 *
	 */
	private class ThreadLoad
	{
		public List<TentacleType> tasks = new ArrayList<TentacleType>();
		private double load = 0;
		
		public void addTask(TentacleType t)
		{
			load += t.getComputationalCost();
			tasks.add(t);
		}
		
		public double getLoad()
		{
			return load;
		}
	}
	
	public ResearchProfileManager(Config config)
	{
		this.nbThread = config.getInt(ConfigInfoKraken.THREAD_NUMBER);
	}
	
	public int addProfile(String mode, List<TentacleType> p, EndOfTrajectoryCheck end)
	{
		List<List<TentacleType>> distribution = new ArrayList<List<TentacleType>>();
		
		List<ThreadLoad> loads = new ArrayList<ThreadLoad>();

		/**
		 * L'objectif est répartir la charge entre les différents threads de manière à minimiser la charge maximale
		 * Une heuristique qui marche : place la prochaine charge sur le thread le moins chargé (algorithme LPT)
		 * Pas super optimisé, mais ce n'est fait qu'une seule fois…
		 */
		
		for(int i = 0; i < nbThread; i++)
		{
			ThreadLoad l = new ThreadLoad();
			loads.add(l);
			distribution.add(l.tasks);
		}
		
		// On trie les tâches de la plus coûteuse à la moins coûteuse
		p.sort(Comparator.comparing(TentacleType::getComputationalCost).reversed());
		
		for(TentacleType t : p)
		{
			loads.sort(Comparator.comparing(ThreadLoad::getLoad));
			loads.get(0).addTask(t); // on ajoute la tâche au thread le moins chargé
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
