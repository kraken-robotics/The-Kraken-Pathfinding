/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles.bezier;

import java.awt.Color;

import pfg.kraken.astar.DirectionStrategy;
import pfg.kraken.astar.tentacles.TentacleComputer;
import pfg.kraken.astar.tentacles.TentacleType;
import pfg.kraken.struct.Kinematic;

/**
 * Interpolation par courbe de Bézier
 * 
 * @author pf
 *
 */

public enum BezierTentacle implements TentacleType
{
	BEZIER_XYOC_TO_XY(2),
	BEZIER_XYOC_TO_XYOC0(3),
//	INTERMEDIATE_BEZIER_XYO_TO_XYO(2), // not used
//	INTERMEDIATE_BEZIER_XYOC_TO_XY(2), // not used
	BEZIER_XYO_TO_XYO(2);

	// order est l'ordre du polynôme de Bézier correspondant (quadratique = 2, etc.)
	private int order;
	public static BezierComputer computer;

	private BezierTentacle(int order)
	{
		this.order = order;
	}
	
	@Override
	public boolean isAcceptable(Kinematic c, DirectionStrategy directionstrategyactuelle, double courbureMax)
	{
		return true;
	}

	@Override
	public int getNbArrets(boolean firstMove)
	{
		return 0;
	}

	@Override
	public Color getColor() {
		return Color.GREEN;
	}

	@Override
	public TentacleComputer getComputer()
	{
		return computer;
	}

	@Override
	public double getComputationalCost()
	{
		return order * (order + 1) / 2;
	}

}
