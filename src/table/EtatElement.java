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

/**
 * Les états de chaque élément de jeu
 * 
 * @author pf
 *
 */

public enum EtatElement
{
	INDEMNE(0), // ces hashs sont utilisés dans la génération du hash de la
				// table
	PRIS_PAR_ENNEMI(1), // ces valeurs ne sont pas choisies au hasard mais sont
						// des masques logiques
	PRIS_PAR_NOUS(3); // en effet, on modifie la valeur du hash par un OU
						// logique
	// De plus, on a l'ordre INDEMNE < PRIS_PAR_ENNEMI < PRIS_PAR_NOUS

	// LE PATHFINDING A LE DROIT DE SHOOTER LES ÉLÉMENTS DE JEU SHOOTÉ PAR
	// L'ENNEMI !

	public final long hash;
	public final int hashBool;
	private static EtatElement reversed[] = new EtatElement[4];

	static
	{
		for(EtatElement t : values())
			reversed[(int) t.hash] = t;
	}

	private EtatElement(int hash)
	{
		this.hash = hash;
		hashBool = hash >> 1;
	}

	public static EtatElement parse(int hash)
	{
		return reversed[hash];
	}

}
