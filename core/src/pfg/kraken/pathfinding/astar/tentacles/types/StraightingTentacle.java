/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.pathfinding.astar.tentacles.types;

import pfg.kraken.pathfinding.astar.DirectionStrategy;
import pfg.kraken.robot.Cinematique;

/**
 * Arc de clothoïde qui ramène le volant au centre
 * 
 * @author pf
 *
 */

public enum StraightingTentacle implements TentacleType
{
	RAMENE_VOLANT(ClothoTentacle.GAUCHE_1, ClothoTentacle.DROITE_1); // ramène le
																	// volant au
																	// centre

	public final ClothoTentacle vitesseGauche, vitesseDroite;

	private StraightingTentacle(ClothoTentacle vitesseGauche, ClothoTentacle vitesseDroite)
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
