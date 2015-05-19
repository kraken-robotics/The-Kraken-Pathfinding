package table;

import scripts.ScriptAnticipableNames;
import scripts.ScriptHookNames;
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
	
	private int[] echecsScripts = new int[ScriptAnticipableNames.values().length];
	private int[] echecsHooks = new int[ScriptHookNames.values().length];
	
    public StrategieInfo(Log log, Config config, StrategieNotifieur notifieur)
    {
    	this.log = log;
    	this.config = config;
    	this.notifieur = notifieur;
    }
    
    /**
     * Un script a été empêché.
     * @param script
     */
    public void incrementeErreur(ScriptAnticipableNames script)
    {
    	echecsScripts[script.ordinal()]++;
    	synchronized(notifieur)
    	{
    		notifieur.notifyAll();
    	}
    }

    /**
     * Un script de hook a été empêché.
     * @param script
     */
    public void incrementeErreur(ScriptHookNames script)
    {
    	echecsHooks[script.ordinal()]++;
    	synchronized(notifieur)
    	{
    		notifieur.notifyAll();
    	}
    }

	@Override
	public void updateConfig()
	{}

}
