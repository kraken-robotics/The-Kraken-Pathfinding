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

package pathfinding.astar.arcs.vitesses;

import pathfinding.DirectionStrategy;
import robot.Cinematique;

/**
 * Arc de clothoïde qui ramène le volant au centre
 * 
 * @author pf
 *
 */

public enum VitesseRameneVolant implements VitesseCourbure
{
	RAMENE_VOLANT(VitesseClotho.GAUCHE_1, VitesseClotho.DROITE_1); // ramène le
																	// volant au
																	// centre

	public final VitesseClotho vitesseGauche, vitesseDroite;

	private VitesseRameneVolant(VitesseClotho vitesseGauche, VitesseClotho vitesseDroite)
	{
		this.vitesseDroite = vitesseDroite;
		this.vitesseGauche = vitesseGauche;
	}

	@Override
	public boolean isAcceptable(Cinematique c, DirectionStrategy directionstrategyactuelle, double courbureMax)
	{
		double courbure = Math.abs(c.courbureGeometrique);
		if(courbure < 0.1 || courbure > 3)
		{
			// log.debug("Ne peut pas ramener le volant si la courbure est déjà
			// nulle ou bien trop grande…");
			return false;
		}

		return true;
	}

	@Override
	public int getNbArrets()
	{
		return 0;
	}
}
