/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles.spin;

import java.awt.Color;
import pfg.kraken.astar.DirectionStrategy;
import pfg.kraken.astar.tentacles.TentacleComputer;
import pfg.kraken.astar.tentacles.TentacleType;
import pfg.kraken.struct.Kinematic;

/**
 * Arc où le robot tourne sur lui-même
 * 
 * @author pf
 *
 */

public enum SpinTentacle implements TentacleType
{
	UP_RIGHT(Math.PI/4),
	UP(Math.PI/2),
	UP_LEFT(3*Math.PI/4),
	LEFT(Math.PI),
	DOWN_LEFT(-3*Math.PI/2),
	DOWN(-Math.PI/2),
	DOWN_RIGHT(-Math.PI/4),
	RIGHT(0),
	ENDPOINT(0),
	HEURISTIC(0);

	public final double angle;
	public static SpinComputer computer;
	
	private SpinTentacle(double angle)
	{
		this.angle = angle;
	}

	@Override
	public TentacleComputer getComputer()
	{
		return computer;
	}

	@Override
	public boolean isAcceptable(Kinematic c, DirectionStrategy directionstrategyactuelle, double courbureMax)
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
	public double getComputationalCost()
	{
		return 0.3;
	}

}
