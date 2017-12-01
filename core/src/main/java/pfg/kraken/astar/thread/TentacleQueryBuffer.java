/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.thread;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import pfg.graphic.log.Log;

/**
 * The buffer of tentacle type that need to be processed
 * @author Pierre-François Gimenez
 *
 */

public class TentacleQueryBuffer
{
	protected Log log;
	private Queue<TentacleTask> buffer = new ConcurrentLinkedQueue<TentacleTask>();
	
	public TentacleQueryBuffer(Log log)
	{
		this.log = log;
	}

	/**
	 * Le buffer est-il vide?
	 * 
	 * @return
	 */
	public boolean isEmpty()
	{
		return buffer.isEmpty();
	}

	/**
	 * Ajout d'un élément dans le buffer et provoque un "notify"
	 * 
	 * @param elem
	 */
	public void add(TentacleTask liste)
	{
//		System.out.println("Ajout !");
		buffer.add(liste);
	}

	/**
	 * Retire un élément du buffer
	 * 
	 * @return
	 */
	public TentacleTask poll()
	{
//		System.out.println("Récupération de "+buffer.peek().index+", encore "+(buffer.size()-1));
		assert !buffer.isEmpty();
		return buffer.poll();
	}
}
