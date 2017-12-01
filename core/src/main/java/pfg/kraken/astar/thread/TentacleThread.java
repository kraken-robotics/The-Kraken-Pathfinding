/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.thread;

import static pfg.kraken.astar.tentacles.Tentacle.PRECISION_TRACE;

import pfg.config.Config;
import pfg.graphic.log.Log;
import pfg.kraken.ConfigInfoKraken;
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
	private NodePool memorymanager;
	private double maxLinearAcceleration;
	private int tempsArret;
	private double deltaSpeedFromStop;
	private static int nbStatic = 0;
	private int nb;
	
	public TentacleThread(Log log, Config config, TentacleQueryBuffer bufferInput, NodePool memorymanager)
	{
		this.log = log;
		this.bufferInput = bufferInput;
		this.memorymanager = memorymanager;
		maxLinearAcceleration = config.getDouble(ConfigInfoKraken.MAX_LINEAR_ACCELERATION);
		deltaSpeedFromStop = Math.sqrt(2 * PRECISION_TRACE * maxLinearAcceleration);
		tempsArret = config.getInt(ConfigInfoKraken.STOP_DURATION);
		nb = nbStatic++;
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
				
				compute(task);
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
				double duration = task.successeur.getArc().getDuree(task.successeur.parent.getArc(), task.vitesseMax, tempsArret, maxLinearAcceleration, deltaSpeedFromStop);
				task.successeur.robot.suitArcCourbe(task.successeur.getArc(), duration);
				task.successeur.g_score = duration;
			}
			else
			{
				memorymanager.destroyNode(task.successeur);
				task.successeur = null;
			}
//			System.out.println("Traité : "+task.index);
			task.done = true;
			task.notify();
		}
	}
}
