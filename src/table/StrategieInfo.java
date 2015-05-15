package table;

import utils.Config;
import utils.Log;
import container.Service;

/**
 * StrategieInfo est à la planification de stratégie
 * ce que l'ObstacleManager est à la recherche de chemin
 * @author pf
 *
 */

public class StrategieInfo implements Service
{
	protected Log log;
	protected Config config;
	
    public StrategieInfo(Log log, Config config)
    {
    	this.log = log;
    	this.config = config;
    }
    
	@Override
	public void updateConfig()
	{}

}
