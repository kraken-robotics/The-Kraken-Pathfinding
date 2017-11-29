/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.thread;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import pfg.graphic.log.Log;
import pfg.kraken.astar.AStarNode;

/**
 * The buffer of tentacles that have been processed
 * @author Pierre-François Gimenez
 *
 */

public class TentacleComputedBuffer
{
	protected Log log;	
	private Queue<AStarNode> buffer = new ConcurrentLinkedQueue<AStarNode>();
	
	public TentacleComputedBuffer(Log log)
	{
		this.log = log;
	}

	/**
	 * Le buffer est-il vide?
	 * 
	 * @return
	 */
	public synchronized boolean isEmpty()
	{
		return buffer.isEmpty();
	}

	/**
	 * Ajout d'un élément dans le buffer et provoque un "notify"
	 * 
	 * @param elem
	 */
	public synchronized void add(AStarNode liste)
	{
		buffer.add(liste);
		notify();
	}

	/**
	 * Retire un élément du buffer
	 * 
	 * @return
	 */
	public synchronized AStarNode poll()
	{
		return buffer.poll();
	}
}
