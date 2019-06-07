/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */


package pfg.kraken.memory;

import pfg.config.Config;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.astar.AStarNode;
import pfg.kraken.obstacles.RobotShape;

/**
 * Memory Manager des nœuds du pathfinding courbe
 * 
 * @author pf
 *
 */

public final class NodePool extends MemoryPool<AStarNode>
{
	private RobotShape vehicleTemplate;
	private EmbodiedKinematicPool pool;

	public NodePool(Config config, RobotShape vehicleTemplate, EmbodiedKinematicPool pool)
	{
		super(AStarNode.class);
		this.pool = pool;
		this.vehicleTemplate = vehicleTemplate;
		init(config.getInt(ConfigInfoKraken.NODE_MEMORY_POOL_SIZE));
	}

	@Override
	protected final void make(AStarNode[] nodes)
	{
		for(int i = 0; i < nodes.length; i++)
			nodes[i] = new AStarNode(vehicleTemplate);
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
			pool.destroy(objet.cameFromArcDynamique.arcs, /*false*/ true);
			objet.cameFromArcDynamique = null;
		}
		super.destroyNode(objet);
	}
}