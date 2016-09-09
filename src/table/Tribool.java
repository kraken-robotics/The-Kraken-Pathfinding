/*
Copyright (C) 2016 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package table;

/**
 * Utilisé on veut qualifier un troisième état pour un booléen: "peut-être".
 * Utilisé quand on interprète le comportement de l'ennemi.
 * @author pf
 *
 */

public enum Tribool {
	FALSE(0), // ces hashs sont utilisés dans la génération du hash de la table
	MAYBE(1), // ces valeurs ne sont pas choisies au hasard mais sont des masques logiques
	TRUE(3); // en effet, on modifie la valeur du hash par un OU logique
	// De plus, on a l'ordre FALSE < MAYBE < TRUE
	
	public final long hash;
	public final int hashBool;
	private static Tribool reversed[] = new Tribool[4];
	
	static
	{
		for(Tribool t: values())
			reversed[(int) t.hash] = t;
	}
	
	private Tribool(int hash)
	{
		this.hash = hash;
		hashBool = hash >> 1;
	}

	public static Tribool parse(int hash)
	{
		return reversed[hash];
	}

}
