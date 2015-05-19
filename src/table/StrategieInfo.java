package table;

import strategie.StrategieNotifieur;
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
	private StrategieNotifieur notifieur;
	
    public StrategieInfo(Log log, Config config, StrategieNotifieur notifieur)
    {
    	this.log = log;
    	this.config = config;
    	this.notifieur = notifieur;
    }
    
	@Override
	public void updateConfig()
	{}

}
