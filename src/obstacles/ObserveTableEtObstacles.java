package obstacles;

import utils.Config;
import utils.Log;
import container.Service;

/**
 * Est notifié par la table et les obstacles.
 * Prévient qu'il faut recalculer un chemin.
 * @author pf
 *
 */

public class ObserveTableEtObstacles implements Service
{
	protected Log log;
	public ObserveTableEtObstacles(Log log)
	{
		this.log = log;
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
