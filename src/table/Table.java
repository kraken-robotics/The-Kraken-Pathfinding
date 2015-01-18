package table;

import obstacles.ObstacleRectangular;
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
	
	/** Contient toutes les informations sur les éléments de jeux sans perte d'information. */
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
	 * On a pris l'objet, on est passé dessus, le robot ennemi est passé dessus...
	 * Attention, on ne peut qu'augmenter cette valeur.
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
	
	/**
	 * Fournit un clone.
	 */
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
	
	/**
	 * Récupération du hash utilisé par l'AStar stratégique
	 * @return
	 */
	public long getHash()
	{
		return hash;
	}

	@Override
	public void updateConfig()
	{}

	/**
	 * g est-il proche de position? (utilisé pour vérifier si on shoot dans un élément de jeu)
	 * @param g
	 * @param position
	 * @param rayon_robot_adverse
	 * @return
	 */
	public boolean isProcheObstacle(GameElementNames g, Vec2 position, int rayon_robot_adverse)
	{
		return total[g.ordinal()].isProcheObstacle(position, rayon_robot_adverse);
	}

	/**
	 * g est-il dans le segment[a, b]?
	 * @param g
	 * @param a
	 * @param b
	 * @return
	 */
	public boolean obstacle_proximite_dans_segment(GameElementNames g, ObstacleRectangular o)
	{
		return o.isColliding(total[g.ordinal()]);
	}

	/**
	 * Utilisé pour l'affichage uniquement.
	 * @return
	 */
	public GameElement[] getObstacles()
	{
		return total;
	}

	/**
	 * Utilisé pour le debug
	 */
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