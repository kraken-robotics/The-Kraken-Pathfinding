/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.thread;

import static pfg.kraken.astar.tentacles.Tentacle.PRECISION_TRACE;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import pfg.config.Config;
import pfg.graphic.log.Log;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.astar.AStarNode;
import pfg.kraken.memory.NodePool;

/**
 * Thread qui calcule des tentacules
 * 
 * @author pf
 *
 */

public class TentacleThread extends Thread
{
	protected Log log;
	private NodePool memorymanager;
	private double maxLinearAcceleration;
	private int tempsArret;
	private double deltaSpeedFromStop;
	private int nb;
	public final Queue<TentacleTask> buffer = new ConcurrentLinkedQueue<TentacleTask>();
	public final Queue<AStarNode> successeurs = new ConcurrentLinkedQueue<AStarNode>();
	public volatile boolean done;
	
	public TentacleThread(Log log, Config config, NodePool memorymanager, int nb)
	{
		this.log = log;
		this.memorymanager = memorymanager;
		maxLinearAcceleration = config.getDouble(ConfigInfoKraken.MAX_LINEAR_ACCELERATION);
		deltaSpeedFromStop = Math.sqrt(2 * PRECISION_TRACE * maxLinearAcceleration);
		tempsArret = config.getInt(ConfigInfoKraken.STOP_DURATION);
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName(getClass().getSimpleName());
		try
		{
			while(true)
			{
				synchronized(this)
				{
					if(buffer.isEmpty())
						wait();
					while(!buffer.isEmpty())
						compute(buffer.poll());
					done = true;
					notify();
				}
			}
		}
		catch(InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

	public void compute(TentacleTask task)
	{
		synchronized(task)
		{
			task.successeur = memorymanager.getNewNode();
	//		assert successeur.cameFromArcDynamique == null;
			task.successeur.cameFromArcDynamique = null;
			task.successeur.parent = task.current;
			
			task.current.robot.copy(task.successeur.robot);
			if(task.computer.compute(task.current, task.v, task.arrivee, task.successeur, nb))
			{
				// Compute the travel time
				int duration = (int) (1000*task.successeur.getArc().getDuree(task.successeur.parent.getArc(), task.vitesseMax, tempsArret, maxLinearAcceleration, deltaSpeedFromStop));
				task.successeur.robot.suitArcCourbe(task.successeur.getArc(), duration);
				task.successeur.g_score = duration;
				successeurs.add(task.successeur);
			}
			else
			{
				memorymanager.destroyNode(task.successeur);
				task.successeur = null;
			}
		}
	}
}
