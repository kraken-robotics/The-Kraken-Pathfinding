package table;

import table.obstacles.ObstacleManager;
import container.Service;
import utils.*;



public class Table implements Service
{


	public ObstacleManager gestionobstacles;

	// Dépendances
	private Log log;
	private Config config;
	
	public Table(Log log, Config config)
	{
		this.log = log;
		this.config = config;
		this.gestionobstacles = new ObstacleManager(log, config);
		initialise();
	}
	
	public void initialise()
	{
	}
	
	//La table
	/**
	 * La table en argument deviendra la copie de this (this reste inchangé)
	 * @param ct
	 */
	public void copy(Table ct) // TODO
	{
        if(!equals(ct))
		{
        	// TODO: faire grande optimisation de ceci a grand coup de hashs
        	
        	
			if(!gestionobstacles.equals(ct.gestionobstacles))
			    gestionobstacles.copy(ct.gestionobstacles);
		}
	}
	
	public Table clone()
	{
		Table cloned_table = new Table(log, config);
		copy(cloned_table);
		return cloned_table;
	}

	/**
	 * Utilisé pour les tests
	 * @param other
	 * @return
	 */
	public boolean equals(Table other)
	{
		return 	false; //TODO
 	}

	@Override
	public void updateConfig() {
		// TODO Auto-generated method stub
		
	}
	

}

