package table;

import obstacles.ObserveTableEtObstacles;
import container.Service;
import enums.Tribool;
import strategie.StrategieNotifieur;
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
	private StrategieNotifieur notifieur;
	private ObserveTableEtObstacles observeur;
	
	/** Contient toutes les informations sur les éléments de jeux sans perte d'information. */
	private volatile long etatTable = 0L;

	/** Version compressée de l'état de la table. Mélange les Tribool FALSE et MAYBE car on ne peut pas passer
	 * de FALSE à MAYBE pendant une recherche de stratégie, donc deux nœuds au hash identiques auront bien
	 * deux etatTable identiques **/
	private volatile int hash = 0;

	public Table(Log log, StrategieNotifieur notifieur, ObserveTableEtObstacles observeur)
	{
		this.log = log;
		this.notifieur = notifieur;
		this.observeur = observeur;
	}

	/**
	 * On a pris l'objet, on est passé dessus, le robot ennemi est passé dessus...
	 * Attention, on ne peut qu'augmenter cette valeur.
	 * @param id
	 */
	public synchronized void setDone(GameElementNames id, Tribool done)
	{
		long old_hash = etatTable;
		etatTable |= (done.hash << (2*id.ordinal()));
		hash |= (done.hashBool << (id.ordinal()));
		// Si besoin est, on dit à la stratégie que la table a été modifiée
		if(old_hash != etatTable)
			synchronized(notifieur)
			{
				notifieur.notify();
				observeur.notify();
			}
	}

	/**
	 * Cet objet est-il présent ou non?
	 * @param id
	 */
	public Tribool isDone(GameElementNames id)
	{
		return Tribool.parse((int)((etatTable >> (2*id.ordinal()))&3));
	}

	/**
	 * La table en argument deviendra la copie de this (this reste inchangé)
	 * @param ct
	 */
	public void copy(Table ct)
	{
		ct.etatTable = etatTable;
		ct.hash = hash;
	}
	
	/**
	 * Fournit un clone.
	 */
	public Table clone()
	{
		Table cloned_table = new Table(log, notifieur, observeur);
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
		return other.etatTable == etatTable;
 	}
	
	@Override
	public int hashCode()
	{
		return hash;
	}
	
	/**
	 * Récupération du hash utilisé par le LPA*
	 * @return
	 */
	public int getHashLPAStar()
	{
		return hash;
	}

	@Override
	public void updateConfig(Config config)
	{}
	
	@Override
	public void useConfig(Config config)
	{}

	/**
	 * Utilisé pour le debug
	 */
	public void printHash()
	{
		for(GameElementNames g: GameElementNames.values)
		{
			long etat = (etatTable >> 2*g.ordinal()) % 4;
			log.debug(g+" : "+Tribool.parse((int)etat));
		}
	}

	/**
	 * Utilisé par les tests
	 * @return
	 */
	public long getEtatTable()
	{
		return etatTable;
	}
	
}