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

package pathfinding;

/**
 * Les différentes vitesses de courbure qu'on peut suivre
 * @author pf
 *
 */

public enum VitesseCourbure
{
	// Ces deux-là sont particuliers. Il ne s'agit pas d'une trajectoire courbe mais juste d'atteindre, en s'arrêtant, tournant puis avançant, l'objectif final
	// En effet, les trajectoires courbes, par manque d'expressivité, peuvent avoir besoin d'une trajectoire directe pour atteindre rapidement la destination
//	DIRECT(0, 0, false), // TODO
//	DIRECT_REBROUSSE(0, 0, true),
	
	// Interpolation cubique avec l'arrivée
	DIRECT_COURBE(0, 0, false),
	DIRECT_COURBE_REBROUSSE(0, 0, true),
	
	GAUCHE_0(1, 1, false),
	GAUCHE_1(4, 2, false),
	GAUCHE_2(9, 3, false),
	GAUCHE_3(16, 4, false),
	COURBURE_IDENTIQUE(0, 0, false),
	DROITE_0(-1, 1, false),
	DROITE_1(-4, 2, false),
	DROITE_2(-9, 3, false),
	DROITE_3(-16, 4, false),
	
	GAUCHE_0_REBROUSSE(1, 1, true),
	GAUCHE_1_REBROUSSE(4, 2, true),
	GAUCHE_2_REBROUSSE(9, 3, true),
	GAUCHE_3_REBROUSSE(16, 4, true),
	COURBURE_IDENTIQUE_REBROUSSE(0, 0, true),
	DROITE_0_REBROUSSE(-1, 1, true),
	DROITE_1_REBROUSSE(-4, 2, true),
	DROITE_2_REBROUSSE(-9, 3, true),
	DROITE_3_REBROUSSE(-16, 4, true);

	public final int vitesse; // vitesse en en m^-1/s
	public final int squaredRootVitesse; // squrt(abs(vitesse))
	public final boolean positif;
	public final boolean rebrousse;
	
	public final static VitesseCourbure[] values;

	static
	{
		values = values();
	}
	
	private VitesseCourbure(int vitesse, int squaredRootVitesse, boolean rebrousse)
	{
		this.rebrousse = rebrousse;
		this.vitesse = vitesse;
		this.positif = vitesse >= 0;
		this.squaredRootVitesse = squaredRootVitesse;
	}
	
}
