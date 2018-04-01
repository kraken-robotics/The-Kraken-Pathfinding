/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.thread;

import static pfg.kraken.astar.tentacles.Tentacle.PRECISION_TRACE;

import java.util.concurrent.BlockingQueue;

import pfg.config.Config;
import pfg.log.Log;
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
	public final BlockingQueue<TentacleTask> buffer;
	public final BlockingQueue<AStarNode> successeurs;
	public final static AStarNode placeholder = new AStarNode();
	
	public TentacleThread(Log log, Config config, NodePool memorymanager, int nb, BlockingQueue<AStarNode> successeurs, BlockingQueue<TentacleTask> buffer)
	{
		this.log = log;
		this.buffer = buffer;
		this.successeurs = successeurs;
		this.memorymanager = memorymanager;
		this.nb = nb;
		maxLinearAcceleration = config.getDouble(ConfigInfoKraken.MAX_LINEAR_ACCELERATION);
		deltaSpeedFromStop = Math.sqrt(2 * PRECISION_TRACE * maxLinearAcceleration);
		tempsArret = config.getInt(ConfigInfoKraken.STOP_DURATION);
		setDaemon(true);
		setPriority(Thread.MAX_PRIORITY);
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName(getClass().getSimpleName()+"-"+nb);
		try
		{
			while(true)
				compute(buffer.take());
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
		AStarNode successeur = memorymanager.getNewNode();
		assert successeur.cameFromArcDynamique == null;
		successeur.parent = task.current;
		
		task.current.robot.copy(successeur.robot);
		if(task.computer.compute(task.current, task.v, task.arrivee, successeur, nb))
		{
			// Compute the travel time
			int duration = (int) (1000*successeur.getArc().getDuree(successeur.parent.getArc(), task.vitesseMax, tempsArret, maxLinearAcceleration, deltaSpeedFromStop));
			successeur.robot.suitArcCourbe(successeur.getArc(), duration);
			successeur.g_score = duration;
			
			successeurs.add(successeur);
		}
		else
		{
			successeurs.add(placeholder);
			memorymanager.destroyNode(successeur);
		}
	}
}
