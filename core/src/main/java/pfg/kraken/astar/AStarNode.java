/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar;

import java.awt.Graphics;

import pfg.kraken.display.Display;
import pfg.kraken.display.Printable;
import pfg.kraken.astar.tentacles.DynamicTentacle;
import pfg.kraken.astar.tentacles.StaticTentacle;
import pfg.kraken.astar.tentacles.Tentacle;
import pfg.kraken.memory.Memorizable;
import pfg.kraken.memory.MemoryPool.MemPoolState;
import pfg.kraken.obstacles.RobotShape;
import pfg.kraken.struct.Kinematic;

/**
 * A node of the A*.
 * 
 * @author pf
 *
 */

public final class AStarNode implements Memorizable, Printable
{
	private static final long serialVersionUID = -2120732124823178009L;
	public Kinematic cinematique; // the cinematic state + the duration since the beginning of the search
	public volatile long date = 0;
	public double g_score; // distance du point de départ à ce point
	public double f_score; // g_score + heuristique = meilleure distance qu'on
							// peut espérer avec ce point
	public AStarNode parent; // the parent of this node (used for reconstruction when a path is found)
	
	/*
	 * If a node has a parent, then we must have the arc between the parent and the node
	 * There are two types of tentacles : fixed-length and random-length.
	 * The fixed-length tentacle is always instantiated (for performance reason)
	 * 
	 * To know with arc to look, checkout the getArc() method
	 */
	public final StaticTentacle cameFromArcStatique; 
	public DynamicTentacle cameFromArcDynamique = null;
	
	/*
	 * Used by the memory pool
	 */
	private volatile int indiceMemoryManager;

	/**
	 * Dummy node
	 */
	public AStarNode()
	{
		cameFromArcStatique = null;
		cinematique = null;
		state = null;
	}
	
	public AStarNode(RobotShape vehicleTemplate)
	{
		cameFromArcStatique = new StaticTentacle(vehicleTemplate);
		this.cinematique = new Kinematic();
	}

	public Tentacle getArc()
	{
		if(parent == null)
			return null;
		if(cameFromArcDynamique != null)
			return cameFromArcDynamique;
		assert cameFromArcStatique != null;
		return cameFromArcStatique;
	}

	/**
	 * Initialisation for the A* (only used for the start point)
	 */
	public void init()
	{
		g_score = Integer.MAX_VALUE;
		f_score = Integer.MAX_VALUE;
		date = 0;
	}

	@Override
	public boolean equals(Object o)
	{
		return o != null && o.hashCode() == hashCode();
	}

	@Override
	public int hashCode()
	{
		return cinematique.hashCode();
	}

	/**
	 * Used by the memory pool
	 */
	@Override
	public void setIndiceMemoryManager(int indice)
	{
		indiceMemoryManager = indice;
	}

	/**
	 * Used by the memory pool
	 */
	@Override
	public int getIndiceMemoryManager()
	{
		return indiceMemoryManager;
	}

	@Override
	public void print(Graphics g, Display f)
	{
		Tentacle a = getArc();
		if(a != null)
			a.print(g, f);
	}
	
	private volatile MemPoolState state = MemPoolState.FREE;
	
	@Override
	public void setState(MemPoolState state)
	{
		this.state = state;
	}
	
	@Override
	public MemPoolState getState()
	{
		return state;
	}
}
