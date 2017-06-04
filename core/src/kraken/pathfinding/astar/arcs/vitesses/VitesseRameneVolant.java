/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package kraken.pathfinding.astar.arcs.vitesses;

import kraken.pathfinding.astar.DirectionStrategy;
import kraken.robot.Cinematique;

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
