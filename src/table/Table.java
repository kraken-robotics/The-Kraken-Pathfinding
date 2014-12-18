package table;

import obstacles.GameElement;
import container.Service;
import enums.GameElementNames;
import enums.Tribool;
import utils.*;

public class Table implements Service
{
	// Dépendances
	private Log log;
	private Config config;
	
	// DEPENDS ON RULES
	
	// Les éléments de jeu de notre couleur.
	private GameElement[] total = new GameElement[20];
	// Et potentiellement les balles de tennis
	
	private static int indice = 0;
	private int hash = 0;
	
	public Table(Log log, Config config)
	{
		this.log = log;
		this.config = config;	
		
		for(GameElementNames n: GameElementNames.values())
			total[n.ordinal()] = new GameElement(log, n);
	}
	
	/**
	 * On a pris l'objet ou on est passé dessus.
	 * @param id
	 */
	public void setDone(GameElementNames id, Tribool done)
	{
		indice++;
		hash = indice;
		total[id.ordinal()].setDone(done);
	}

	/**
	 * Cet objet est-il présent ou non?
	 * @param id
	 */
	public Tribool isDone(GameElementNames id)
	{
		return total[id.ordinal()].isDone();
	}	

	/**
	 * La table en argument deviendra la copie de this (this reste inchangé)
	 * @param ct
	 */
	public void copy(Table ct)
	{
		if(hash != ct.hash)
			for(int i = 0; i < 20; i++)
				total[i].fastClone(ct.total[i]);
		ct.hash = hash;
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
		return other.hash == hash;
 	}
	
	public int getHash()
	{
		return hash;
	}

	@Override
	public void updateConfig()
	{
	}
	
	/**
	 * Utilisé par l'obstacle manager
	 * @return
	 */
	public GameElement[] getObstacles()
	{
		return total;
	}

}