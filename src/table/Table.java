package table;

import container.Service;
import utils.*;

public class Table implements Service
{
	// Dépendances
	private Log log;
	private Config config;
	
	public Table(Log log, Config config)
	{
		this.log = log;
		this.config = config;
		
		// TODO initialisation des éléments de jeux
	}
	
	/**
	 * La table en argument deviendra la copie de this (this reste inchangé)
	 * @param ct
	 */
	public void copy(Table ct) // TODO
	{
        if(!equals(ct))
		{
        	// TODO: faire grande optimisation de ceci a grand coup de hashs
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

