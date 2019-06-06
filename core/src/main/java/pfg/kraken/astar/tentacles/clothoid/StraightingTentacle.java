/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles.clothoid;

import java.awt.Color;

import pfg.kraken.astar.DirectionStrategy;
import pfg.kraken.astar.tentacles.TentacleComputer;
import pfg.kraken.astar.tentacles.TentacleType;
import pfg.kraken.struct.Kinematic;

/**
 * Arc de clothoïde qui ramène le volant au centre
 * FIXME UNUSED
 * @author pf
 *
 */

public enum StraightingTentacle implements TentacleType
{
	STRAIGHT(ClothoTentacle.LEFT_1, ClothoTentacle.RIGHT_1); // ramène le
																	// volant au
																	// centre

	public final ClothoTentacle vitesseGauche, vitesseDroite;
	public static ClothoidComputer computer;

	private StraightingTentacle(ClothoTentacle vitesseGauche, ClothoTentacle vitesseDroite)
	{
		this.vitesseDroite = vitesseDroite;
		this.vitesseGauche = vitesseGauche;
	}

	@Override
	public boolean isAcceptable(Kinematic c, DirectionStrategy directionstrategyactuelle, double courbureMax)
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
	public TentacleComputer getComputer()
	{
		return computer;
	}
	
	@Override
	public double getComputationalCost()
	{
		return 1;
	}

}
