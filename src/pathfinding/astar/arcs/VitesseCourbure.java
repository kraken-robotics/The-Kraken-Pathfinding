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

package pathfinding.astar.arcs;

/**
 * Les différentes vitesses de courbure qu'on peut suivre
 * @author pf
 *
 */

public enum VitesseCourbure
{
	// Vitesse maximale AX-12A : 0.8 tours à la seconde (50 RPM)
	// TODO en déduire la dérivée de la courbure max
	
	// Interpolation cubique avec l'arrivée
	DIRECT_COURBE(0),
	DIRECT_COURBE_REBROUSSE(0),
	
	GAUCHE_0(1),
	GAUCHE_1(4),
	GAUCHE_2(9),
	GAUCHE_3(16),
	COURBURE_IDENTIQUE(0),
	DROITE_0(-1),
	DROITE_1(-4),
	DROITE_2(-9),
	DROITE_3(-16),
	
//	GAUCHE_0_REBROUSSE(1),
//	GAUCHE_1_REBROUSSE(4),
//	GAUCHE_2_REBROUSSE(9),
	GAUCHE_3_REBROUSSE(16),
	COURBURE_IDENTIQUE_REBROUSSE(0),
//	DROITE_0_REBROUSSE(-1);
//	DROITE_1_REBROUSSE(-4),
//	DROITE_2_REBROUSSE(-9),
	DROITE_3_REBROUSSE(-16);

	public final int vitesse; // vitesse en en m^-1/s
	public final int squaredRootVitesse; // sqrt(abs(vitesse))
	public final boolean positif;
	public final boolean rebrousse;
	
	private VitesseCourbure(int vitesse)
	{
		this.rebrousse = toString().contains("REBROUSSE");
		this.vitesse = vitesse;
		this.positif = vitesse >= 0;
		
		this.squaredRootVitesse = (int) Math.sqrt(Math.abs(vitesse));
	}
	
}
