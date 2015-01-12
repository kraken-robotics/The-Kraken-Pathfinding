package threads;

import table.ObstacleManager;
import table.Table;
import utils.Config;
import utils.Log;
import container.Service;

/**
 * Thread d'analyse de l'ennemi.
 * En fonction de la position de l'ennemi, on imagine les actions qu'il fait
 * @author pf
 *
 */

public class ThreadAnalyseEnnemi extends AbstractThread implements Service {

	// TODO quand on aura une balise 
	
/*	private Log log;
	private Config config;
	private ObstacleManager obstaclemanager;
	private Table table;*/

	public ThreadAnalyseEnnemi(Log log, Config config, ObstacleManager obstaclemanager, Table table)
	{
/*		this.log = log;
		this.config = config;
		this.obstaclemanager = obstaclemanager;
		this.table = table;*/

		Thread.currentThread().setPriority(2);
		updateConfig();
	}

	@Override
	public void updateConfig()
	{
	}

}
