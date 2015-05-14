package planification.dstar;

import java.util.ArrayList;

import utils.Config;
import utils.Log;
import container.Service;

/**
 * DStarLite. Uniquement pour la recherche de chemin pour le moment.
 * @author pf
 * 
 */

public class DStarLite implements Service {
	
	protected Log log;
	protected Config config;
	
	public DStarLite(Log log, Config config)
	{
		this.log = log;
		this.config = config;
	}
	
	public ArrayList<LocomotionNode> computePath(GridPoint depart, GridPoint arrivee)
	{
		return null;
	}

	@Override
	public void updateConfig()
	{}

}
