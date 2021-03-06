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
 * Arc de clothoïde qui fait un demi-tour
 * FIXME UNUSED
 * @author pf
 *
 */

public enum TurnoverTentacle implements TentacleType
{
	RIGHT_TURN_OVER(ClothoTentacle.RIGHT_2),
	LEFT_TURN_OVER(ClothoTentacle.LEFT_2);

	public ClothoTentacle v;
	public static ClothoidComputer computer;

	private TurnoverTentacle(ClothoTentacle v)
	{
		this.v = v;
	}

	@Override
	public boolean isAcceptable(Kinematic c, DirectionStrategy directionstrategyactuelle, double courbureMax)
	{
		// on évite les demi-tours absurdes
		if(((v.positif && c.courbureGeometrique < -1) || (!v.positif && c.courbureGeometrique > 1)))
			return false;

		return true;
	}

	@Override
	public int getNbArrets(boolean firstMove)
	{
		return 1;
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
		return 2;
	}

}
