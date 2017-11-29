/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.thread;

import static pfg.kraken.astar.tentacles.Tentacle.PRECISION_TRACE;

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
	private TentacleQueryBuffer bufferInput;
	private TentacleComputedBuffer bufferOutput;
	private NodePool memorymanager;
	private double maxLinearAcceleration;
	private int tempsArret;
	private double deltaSpeedFromStop;

	public TentacleThread(Log log, Config config, TentacleQueryBuffer bufferInput, TentacleComputedBuffer bufferOutput, NodePool memorymanager)
	{
		this.log = log;
		this.bufferInput = bufferInput;
		this.bufferOutput = bufferOutput;
		this.memorymanager = memorymanager;
		maxLinearAcceleration = config.getDouble(ConfigInfoKraken.MAX_LINEAR_ACCELERATION);
		deltaSpeedFromStop = Math.sqrt(2 * PRECISION_TRACE * maxLinearAcceleration);
		tempsArret = config.getInt(ConfigInfoKraken.STOP_DURATION);

	}

	@Override
	public void run()
	{
		Thread.currentThread().setName(getClass().getSimpleName());
		TentacleTask task;
		try
		{
			while(true)
			{
				synchronized(bufferInput)
				{
					task = null;
					do {
						// wait only if necessary
						if(bufferInput.isEmpty())
							bufferInput.wait();
						
						// other process are awakened : there might be no task left
						if(!bufferInput.isEmpty())
							task = bufferInput.poll();
					} while(task == null);
				}
				
				AStarNode successeur = compute(task);
				if(successeur != null)
					bufferOutput.add(successeur);
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

	public AStarNode compute(TentacleTask task)
	{
		AStarNode successeur = memorymanager.getNewNode();
//		assert successeur.cameFromArcDynamique == null;
		successeur.cameFromArcDynamique = null;
		successeur.parent = task.current;
		
		task.current.robot.copy(successeur.robot);
		if(task.computer.compute(task.current, task.v, task.arrivee, successeur))
		{
			// Compute the travel time
			double duration = successeur.getArc().getDuree(successeur.parent.getArc(), task.vitesseMax, tempsArret, maxLinearAcceleration, deltaSpeedFromStop);
			successeur.robot.suitArcCourbe(successeur.getArc(), duration);
			successeur.g_score = duration;
			return successeur;
		}
		return null;
	}
}
