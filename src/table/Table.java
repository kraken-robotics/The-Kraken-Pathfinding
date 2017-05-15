/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package table;

import utils.*;

/**
 * Gère les éléments de jeux
 * 
 * @author pf
 *
 */

public class Table
{
	// Dépendances
	protected Log log;

	/**
	 * Contient toutes les informations sur les éléments de jeux sans perte
	 * d'information.
	 */
	protected volatile long etatTable = 0L;

	public Table(Log log)
	{
		this.log = log;
	}

	/**
	 * On a pris l'objet, on est passé dessus, le robot ennemi est passé
	 * dessus...
	 * Attention, on ne peut qu'augmenter cette valeur.
	 * Renvoie vrai si l'état de la table a changé pour le futur (?)
	 * 
	 * @param id
	 */
	public synchronized boolean setDone(GameElementNames id, EtatElement done)
	{
		long old_hash = etatTable;
		etatTable |= (done.hash << (2 * id.ordinal()));
		// Si besoin est, on dit à la stratégie que la table a été modifiée
		return old_hash != etatTable;
	}

	/**
	 * Cet objet est-il présent ou non?
	 * 
	 * @param id
	 */
	public EtatElement isDone(GameElementNames id)
	{
		return isDone(id, etatTable);
	}

	/**
	 * Cet objet est-il présent ou non?
	 * 
	 * @param id
	 */
	protected EtatElement isDone(GameElementNames id, long etat)
	{
		return EtatElement.parse((int) ((etat >> (2 * id.ordinal())) & 3));
	}

	/**
	 * La table en argument deviendra la copie de this (this reste inchangé)
	 * 
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
}