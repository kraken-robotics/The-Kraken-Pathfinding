/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles.types;

import java.awt.Color;

import pfg.kraken.astar.DirectionStrategy;
import pfg.kraken.astar.tentacles.BezierComputer;
import pfg.kraken.astar.tentacles.TentacleComputer;
import pfg.kraken.robot.Cinematique;

/**
 * Interpolation par courbe de Bézier
 * 
 * @author pf
 *
 */

public enum BezierTentacle implements TentacleType
{
	BEZIER_XYOC_TO_XY(2),
	BEZIER_XYOC_TO_XYO(3),
	BEZIER_XYO_TO_XYO(2);

	// order est l'ordre du polynôme de Bézier correspondant (quadratique = 2, etc.)
	private int order;

	private BezierTentacle(int order)
	{
		this.order = order;
	}
	
	@Override
	public boolean isAcceptable(Cinematique c, DirectionStrategy directionstrategyactuelle, double courbureMax)
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
	public Class<? extends TentacleComputer> getComputer()
	{
		return BezierComputer.class;
	}

	@Override
	public double getComputationalCost()
	{
		return order * (order + 1) / 2;
	}

}
