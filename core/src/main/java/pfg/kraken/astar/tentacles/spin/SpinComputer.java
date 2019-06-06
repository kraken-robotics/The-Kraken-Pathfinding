/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles.spin;

import java.util.ArrayList;
import java.util.List;
import pfg.config.Config;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.astar.AStarNode;
import pfg.kraken.astar.tentacles.DynamicTentacle;
import pfg.kraken.astar.tentacles.TentacleComputer;
import pfg.kraken.astar.tentacles.TentacleType;
import pfg.kraken.memory.EmbodiedKinematicPool;
import pfg.kraken.struct.Kinematic;
import pfg.kraken.struct.EmbodiedKinematic;

/**
 * A simple computer
 * @author pf
 *
 */

public final class SpinComputer implements TentacleComputer
{
	private EmbodiedKinematicPool memory;
	private double rootedMaxAcceleration;
	
	public SpinComputer(EmbodiedKinematicPool memory, Config config)
	{
		this.memory = memory;
		rootedMaxAcceleration = Math.sqrt(config.getDouble(ConfigInfoKraken.MAX_LATERAL_ACCELERATION));
	}
	
	@Override
	public boolean compute(AStarNode current, TentacleType tentacleType, Kinematic arrival, AStarNode modified, int indexThread)
	{
		assert tentacleType instanceof SpinTentacle : tentacleType;
	
		SpinTentacle t = (SpinTentacle) tentacleType;
		
		List<EmbodiedKinematic> l = new ArrayList<EmbodiedKinematic>();
		EmbodiedKinematic c = memory.getNewNode();
		
		Kinematic cinemInitiale = current.cinematique;
		c.update(cinemInitiale.getX(),
				cinemInitiale.getY(),
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
