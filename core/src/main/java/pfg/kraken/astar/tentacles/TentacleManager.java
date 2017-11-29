/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import pfg.config.Config;
import pfg.injector.Injector;
import pfg.injector.InjectorException;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.LogCategoryKraken;
import pfg.kraken.SeverityCategoryKraken;
import pfg.kraken.astar.AStarNode;
import pfg.kraken.astar.DirectionStrategy;
import pfg.kraken.astar.tentacles.types.TentacleType;
import pfg.kraken.astar.thread.TentacleComputedBuffer;
import pfg.kraken.astar.thread.TentacleQueryBuffer;
import pfg.kraken.astar.thread.TentacleTask;
import pfg.kraken.astar.thread.TentacleThread;
import pfg.kraken.dstarlite.DStarLite;
import pfg.kraken.memory.NodePool;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.container.DynamicObstacles;
import pfg.kraken.obstacles.container.StaticObstacles;
import pfg.kraken.robot.Cinematique;
import pfg.kraken.utils.XY;
import pfg.graphic.log.Log;

/**
 * Réalise des calculs pour l'A* courbe.
 * 
 * @author pf
 *
 */

public class TentacleManager implements Iterable<AStarNode>, Iterator<AStarNode>
{
	protected Log log;
	private DStarLite dstarlite;
	private DynamicObstacles dynamicObs;
	private TentacleQueryBuffer bufferInput;
	private TentacleComputedBuffer bufferOutput;
	
	private double courbureMax, vitesseMax;
	private Injector injector;
	private StaticObstacles fixes;
	private TentacleThread[] threads;
	
	private DirectionStrategy directionstrategyactuelle;
	private Cinematique arrivee = new Cinematique();
//	private ResearchProfileManager profiles;
	private List<TentacleType> currentProfile = new ArrayList<TentacleType>();
	private List<AStarNode> successeurs = new ArrayList<AStarNode>();
//	private List<StaticObstacles> disabledObstaclesFixes = new ArrayList<StaticObstacles>();

	public TentacleManager(Log log, StaticObstacles fixes, DStarLite dstarlite, Config config, TentacleQueryBuffer bufferInput, TentacleComputedBuffer bufferOutput, DynamicObstacles dynamicObs, Injector injector, ResearchProfileManager profiles, NodePool memorymanager) throws InjectorException
	{
		this.bufferInput = bufferInput;
		this.bufferOutput = bufferOutput;
		this.injector = injector;
		this.fixes = fixes;
		this.dynamicObs = dynamicObs;
		this.log = log;
		this.dstarlite = dstarlite;
		
		this.currentProfile = profiles.getProfile(0);
		for(TentacleType t : currentProfile)
			injector.getService(t.getComputer());
		
		int nbThreads = config.getInt(ConfigInfoKraken.THREAD_NUMBER);
		
		threads = new TentacleThread[nbThreads];
		for(int i = 0; i < nbThreads; i++)
		{
			threads[i] = new TentacleThread(log, config, bufferInput, bufferOutput, memorymanager);
			if(nbThreads != 1)
				threads[i].start();
		}
		
		courbureMax = config.getDouble(ConfigInfoKraken.MAX_CURVATURE);
		coins[0] = fixes.getBottomLeftCorner();
		coins[2] = fixes.getTopRightCorner();
		coins[1] = new XY(coins[0].getX(), coins[2].getY());
		coins[3] = new XY(coins[2].getX(), coins[0].getY());
	}

	private XY[] coins = new XY[4];
	
	/**
	 * Retourne faux si un obstacle est sur la route
	 * 
	 * @param node
	 * @return
	 * @throws FinMatchException
	 */
	public boolean isReachable(AStarNode node)
	{
		// le tout premier nœud n'a pas de parent
		if(node.parent == null)
			return true;

		int nbOmbres = node.getArc().getNbPoints();
		
		// On vérifie la collision avec les murs
		for(int j = 0; j < nbOmbres; j++)
			for(int i = 0; i < 4; i++)
				if(node.getArc().getPoint(j).obstacle.isColliding(coins[i], coins[(i+1)&3]))
					return false;
		
		// Collision avec un obstacle fixe?
		for(Obstacle o : fixes.getObstacles())
			for(int i = 0; i < nbOmbres; i++)
				if(/*!disabledObstaclesFixes.contains(o) && */o.isColliding(node.getArc().getPoint(i).obstacle))
				{
					// log.debug("Collision avec "+o);
					return false;
				}

		// Collision avec un obstacle de proximité ?

		try {
			Iterator<Obstacle> iter = dynamicObs.getFutureDynamicObstacles(0); // TODO date !
			while(iter.hasNext())
			{
				Obstacle n = iter.next();
				for(int i = 0; i < nbOmbres; i++)
					if(n.isColliding(node.getArc().getPoint(i).obstacle))
					{
						// log.debug("Collision avec un obstacle de proximité.");
						return false;
					}
			}
		} catch(NullPointerException e)
		{
			log.write(e.toString(), SeverityCategoryKraken.CRITICAL, LogCategoryKraken.PF);
		}
		/*
		 * node.state.iterator.reinit();
		 * while(node.state.iterator.hasNext())
		 * if(node.state.iterator.next().isColliding(obs))
		 * {
		 * // log.debug("Collision avec un obstacle de proximité.");
		 * return false;
		 * }
		 */

		return true;
	}

	/**
	 * Initialise l'arc manager avec les infos donnée
	 * 
	 * @param directionstrategyactuelle
	 * @param sens
	 * @param arrivee
	 */
	public void configure(DirectionStrategy directionstrategyactuelle, double vitesseMax, Cinematique arrivee)
	{
		this.vitesseMax = vitesseMax;
		this.directionstrategyactuelle = directionstrategyactuelle;
		arrivee.copy(this.arrivee);
	}

	public void configure(DirectionStrategy directionstrategyactuelle, double vitesseMax, XY arrivee)
	{
		this.vitesseMax = vitesseMax;
		this.directionstrategyactuelle = directionstrategyactuelle;
		this.arrivee.updateReel(arrivee.getX(), arrivee.getY(), 0, true, 0);
	}

	/**
	 * Réinitialise l'itérateur à partir d'un nouvel état
	 * 
	 * @param current
	 * @param directionstrategyactuelle
	 */
	public void computeTentacles(AStarNode current)
	{
		successeurs.clear();
		assert bufferInput.isEmpty();
		for(TentacleType v : currentProfile)
		{
			if(v.isAcceptable(current.robot.getCinematique(), directionstrategyactuelle, courbureMax))
			{
				TentacleTask tt = new TentacleTask();
				tt.arrivee = arrivee;
				tt.current = current;
				tt.v = v;
				tt.computer = injector.getExistingService(v.getComputer());
				tt.vitesseMax = vitesseMax;
				
				if(threads.length == 1)
				{
					AStarNode node = threads[0].compute(tt);
					if(node != null)
						bufferOutput.add(node);
				}
				else
					bufferInput.add(tt);
			}
		}
	}

	public synchronized Double heuristicCostCourbe(Cinematique c)
	{
		if(dstarlite.heuristicCostCourbe(c) == null)
			return null;
		return dstarlite.heuristicCostCourbe(c) / vitesseMax;
	}

	public boolean isArrived(AStarNode successeur)
	{
		return successeur.getArc() != null && successeur.getArc().getLast().getPosition().squaredDistance(arrivee.getPosition()) < 5;
	}

	@Override
	public Iterator<AStarNode> iterator()
	{
		return this;
	}

	@Override
	public boolean hasNext()
	{
		return !bufferInput.isEmpty() || !bufferOutput.isEmpty();
	}
	
	@Override
	public AStarNode next()
	{
		if(bufferOutput.isEmpty())
		{
			synchronized(bufferOutput)
			{
				if(bufferOutput.isEmpty())
				{
					try {
						bufferOutput.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
						return null;
					}
				}
			}
		}

		assert !bufferOutput.isEmpty();
		return bufferOutput.poll();
	}
}
