package table;

import obstacles.gameElement.GameElement;
import obstacles.gameElement.GameElementNames;
import container.Service;
import enums.Tribool;
import utils.*;

/**
 * Gère les éléments de jeux
 * @author pf
 *
 */

public class Table implements Service
{
	// Dépendances
	private Log log;
	private Config config;
	
	// Les éléments de jeu de notre couleur.
	private static GameElement[] total = new GameElement[GameElementNames.values().length];
	// Et potentiellement les balles de tennis
	
	// Le hash est exact; pas de collisions possibles.
	// La valeur initiale 0 provient des hash des Tribool.
	private long hash = 0;
	
	public Table(Log log, Config config)
	{
		this.log = log;
		this.config = config;	
		
		for(GameElementNames n: GameElementNames.values())
			total[n.ordinal()] = new GameElement(log, config, n);
		
		updateConfig();
	}
	
	/**
	 * On a pris l'objet ou on est passé dessus.
	 * @param id
	 */
	public void setDone(GameElementNames id, Tribool done)
	{
		hash |= (done.getHash() << (2*id.ordinal()));
	}

	/**
	 * Cet objet est-il présent ou non?
	 * @param id
	 */
	public Tribool isDone(GameElementNames id)
	{
		return Tribool.parse((int)((hash >> (2*id.ordinal()))&3));
	}

	/**
	 * La table en argument deviendra la copie de this (this reste inchangé)
	 * @param ct
	 */
	public void copy(Table ct)
	{
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
	
	public long getHash()
	{
		return hash;
	}

	@Override
	public void updateConfig()
	{}

	public boolean isProcheObstacle(GameElementNames g, Vec2 position, int rayon_robot_adverse)
	{
		return total[g.ordinal()].isProcheObstacle(position, rayon_robot_adverse);
	}

	public boolean obstacle_proximite_dans_segment(GameElementNames g, Vec2 a, Vec2 b)
	{
		return total[g.ordinal()].obstacle_proximite_dans_segment_dilatation(a, b);
	}

	/**
	 * Utilisé pour l'affichage uniquement.
	 * @return
	 */
	public GameElement[] getObstacles()
	{
		return total;
	}

	public void printHash()
	{
		for(GameElementNames g: GameElementNames.values())
		{
			long etat = (hash >> 2*g.ordinal()) % 4;
			for(Tribool t: Tribool.values())
				if(etat == t.getHash())
					log.debug(g+": "+t, this);
		}
	}

}