/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles;

import java.util.ArrayList;
import java.util.List;
import pfg.config.Config;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.astar.AStarNode;
import pfg.kraken.astar.tentacles.types.SpinTentacle;
import pfg.kraken.astar.tentacles.types.TentacleType;
import pfg.kraken.memory.CinemObsPool;
import pfg.kraken.robot.Cinematique;
import pfg.kraken.robot.CinematiqueObs;

/**
 * A simple computer
 * @author pf
 *
 */

public final class SpinComputer implements TentacleComputer
{
	private CinemObsPool memory;
	private double rootedMaxAcceleration;
	
	public SpinComputer(CinemObsPool memory, Config config)
	{
		this.memory = memory;
		rootedMaxAcceleration = Math.sqrt(config.getDouble(ConfigInfoKraken.MAX_LATERAL_ACCELERATION));
	}
	
	@Override
	public boolean compute(AStarNode current, TentacleType tentacleType, Cinematique arrival, AStarNode modified, int indexThread)
	{
		assert tentacleType instanceof SpinTentacle : tentacleType;
	
		SpinTentacle t = (SpinTentacle) tentacleType;
		
		List<CinematiqueObs> l = new ArrayList<CinematiqueObs>();
		CinematiqueObs c = memory.getNewNode();
		
		Cinematique cinemInitiale = current.cinematique;
		c.update(cinemInitiale.getPosition().getX(),
				cinemInitiale.getPosition().getY(),
				t.angle,
				true,
				0,
				rootedMaxAcceleration,
				true);
		l.add(c);
		modified.cameFromArcDynamique = new DynamicTentacle(l, t);
		
		return true;
	}

}
