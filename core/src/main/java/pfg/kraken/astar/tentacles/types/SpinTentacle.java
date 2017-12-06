/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles.types;

import java.awt.Color;
import pfg.kraken.astar.DirectionStrategy;
import pfg.kraken.astar.ResearchMode;
import pfg.kraken.astar.tentacles.SpinComputer;
import pfg.kraken.astar.tentacles.TentacleComputer;
import pfg.kraken.robot.Cinematique;

/**
 * Arc où le robot tourne sur lui-même
 * 
 * @author pf
 *
 */

public enum SpinTentacle implements TentacleType
{
	UP(Math.PI/2),
	DOWN(-Math.PI/2),
	RIGHT(Math.PI),
	LEFT(0);

	public final double angle;
	
	private SpinTentacle(double angle)
	{
		this.angle = angle;
	}

	@Override
	public Class<? extends TentacleComputer> getComputer()
	{
		return SpinComputer.class;
	}

	@Override
	public boolean isAcceptable(Cinematique c, DirectionStrategy directionstrategyactuelle, double courbureMax)
	{
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
		return Color.ORANGE;
	}

	@Override
	public boolean usableFor(ResearchMode mode)
	{
		return true;
	}

}
