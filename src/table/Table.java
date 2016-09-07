package table;

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
	
	/** Contient toutes les informations sur les éléments de jeux sans perte d'information. */
	private volatile long etatTable = 0L;

	public Table(Log log)
	{
		this.log = log;
	}

	/**
	 * On a pris l'objet, on est passé dessus, le robot ennemi est passé dessus...
	 * Attention, on ne peut qu'augmenter cette valeur.
	 * Renvoie vrai si l'état de la table a changé pour le futur (?)
	 * @param id
	 */
	public synchronized boolean setDone(GameElementNames id, Tribool done)
	{
		long old_hash = etatTable;
		etatTable |= (done.hash << (2*id.ordinal()));
		// Si besoin est, on dit à la stratégie que la table a été modifiée
		return old_hash != etatTable;
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
	}
	
	/**
	 * Fournit un clone.
	 */
	@Override
	public Table clone()
	{
		Table cloned_table = new Table(log);
		copy(cloned_table);
		return cloned_table;
	}

	@Override
	public void updateConfig(Config config)
	{}
	
	@Override
	public void useConfig(Config config)
	{}
	
	/**
	 * Utilisé par les tests
	 * @return
	 */
	public long getEtatTable()
	{
		return etatTable;
	}
	
}