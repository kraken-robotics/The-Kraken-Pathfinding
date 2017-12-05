/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles.types;

import java.awt.Color;

import pfg.kraken.astar.DirectionStrategy;
import pfg.kraken.astar.ResearchMode;
import pfg.kraken.astar.tentacles.ClothoidesComputer;
import pfg.kraken.astar.tentacles.TentacleComputer;
import pfg.kraken.robot.Cinematique;

/**
 * Arc de clothoïde qui ramène le volant au centre
 * FIXME UNUSED
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
	public int getNbArrets(boolean firstMove)
	{
		return 0;
	}

	@Override
	public Color getColor()
	{
		return Color.GRAY;
	}

	@Override
	public Class<? extends TentacleComputer> getComputer()
	{
		return ClothoidesComputer.class;
	}

	@Override
	public boolean usableFor(ResearchMode mode)
	{
		return false;
	}
}
