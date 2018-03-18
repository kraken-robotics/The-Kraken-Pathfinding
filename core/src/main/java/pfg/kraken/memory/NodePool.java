/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */


package pfg.kraken.memory;

import pfg.config.Config;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.astar.AStarNode;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.robot.RobotState;
import pfg.log.Log;

/**
 * Memory Manager des nœuds du pathfinding courbe
 * 
 * @author pf
 *
 */

public class NodePool extends MemoryPool<AStarNode>
{
	private RectangularObstacle vehicleTemplate;
	private CinemObsPool pool;

	public NodePool(Log log, Config config, RectangularObstacle vehicleTemplate, CinemObsPool pool)
	{
		super(AStarNode.class, log);
		this.pool = pool;
		this.vehicleTemplate = vehicleTemplate;
		init(config.getInt(ConfigInfoKraken.NODE_MEMORY_POOL_SIZE));
	}

	@Override
	protected final void make(AStarNode[] nodes)
	{
		for(int i = 0; i < nodes.length; i++)
			nodes[i] = new AStarNode(new RobotState(), vehicleTemplate);
	}

	@Override
	public synchronized AStarNode getNewNode()
	{
		AStarNode out = super.getNewNode();
		out.cameFromArcDynamique = null;
		return out;
	}
	
	@Override
	public synchronized void destroyNode(AStarNode objet)
	{
		if(objet.cameFromArcDynamique != null)
		{
			pool.destroy(objet.cameFromArcDynamique.arcs, false);
			objet.cameFromArcDynamique = null;
		}
		super.destroyNode(objet);
	}
}