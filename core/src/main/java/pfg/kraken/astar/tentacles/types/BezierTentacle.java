/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles.types;

import java.awt.Color;

import pfg.kraken.astar.DirectionStrategy;
import pfg.kraken.astar.ResearchMode;
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
	BEZIER_XYOC_TO_XY(0, ResearchMode.XYO2XY),
	BEZIER_XYOC_TO_XYO(0, ResearchMode.XYO2XYO);

	private final int nbArrets;
	private final ResearchMode mode;

	private BezierTentacle(int nbArrets, ResearchMode mode)
	{
		this.mode = mode;
		this.nbArrets = nbArrets;
	}
	
	@Override
	public boolean isAcceptable(Cinematique c, DirectionStrategy directionstrategyactuelle, double courbureMax)
	{
		return true;
	}

	@Override
	public int getNbArrets(boolean firstMove)
	{
		// TODO : utiliser firstMove
		return nbArrets;
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
	public boolean usableFor(ResearchMode mode)
	{
		return mode == this.mode;
	}
}
