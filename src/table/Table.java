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
	
	/**
	 * Utilisé pour les tests
	 * @param other
	 * @return
	 */
	public boolean equals(Table other)
	{
		return false; //TODO
 	}

	@Override
	public void updateConfig() {
		// TODO Auto-generated method stub
		
	}
	

}

