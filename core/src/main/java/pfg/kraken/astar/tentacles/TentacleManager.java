/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import pfg.config.Config;
import pfg.injector.InjectorException;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.astar.AStarNode;
import pfg.kraken.astar.DirectionStrategy;
import pfg.kraken.astar.engine.PhysicsEngine;
import pfg.kraken.astar.profiles.ResearchProfile;
import pfg.kraken.astar.profiles.ResearchProfileManager;
import pfg.kraken.astar.tentacles.computethread.TentacleTask;
import pfg.kraken.astar.tentacles.computethread.TentacleThread;
import pfg.kraken.dstarlite.DStarLite;
import pfg.kraken.exceptions.UnknownModeException;
import pfg.kraken.memory.NodePool;
import pfg.kraken.struct.Kinematic;
import pfg.kraken.struct.XY;
import pfg.kraken.struct.EmbodiedKinematic;
import pfg.kraken.struct.XYO;
import pfg.kraken.display.ColorKraken;
import pfg.kraken.display.Display;
import pfg.kraken.display.Layer;
import static pfg.kraken.astar.tentacles.Tentacle.*;

/**
 * Réalise des calculs pour l'A* courbe.
 * 
 * @author pf
 *
 */

public final class TentacleManager implements Iterator<AStarNode>
{
	private DStarLite dstarlite;
	private double courbureMax, vitesseMax;
	private boolean printObstacles;
	private Display display;
	private TentacleThread[] threads;
	private ResearchProfile currentProfile;
	private Map<TentacleType, Integer> tentaclesStats = new HashMap<TentacleType, Integer>();
	
	private DirectionStrategy directionstrategyactuelle;
	private Kinematic arrivee = new Kinematic();
	private ResearchProfileManager profiles;
	private List<TentacleTask> tasks = new ArrayList<TentacleTask>();
	private BlockingQueue<AStarNode> successeurs = new LinkedBlockingQueue<AStarNode>();
	private BlockingQueue<TentacleTask> buffer = new LinkedBlockingQueue<TentacleTask>();
	private PhysicsEngine engine;
	
	private int nbLeft;
	private double squaredImmunityCircle;
	private XY startPosition;
	
	public TentacleManager(NodePool memorymanager, PhysicsEngine engine, DStarLite dstarlite, Config config, ResearchProfileManager profiles, Display display) throws InjectorException
	{
		this.dstarlite = dstarlite;
		this.display = display;
		this.profiles = profiles;
		this.engine = engine;
		
		for(int i = 0; i < 100; i++)
			tasks.add(new TentacleTask());
	
		printObstacles = config.getBoolean(ConfigInfoKraken.GRAPHIC_ROBOT_COLLISION);
		int nbThreads = config.getInt(ConfigInfoKraken.THREAD_NUMBER);
		
		squaredImmunityCircle = config.getDouble(ConfigInfoKraken.OBSTACLE_IMMUNITY_CIRCLE);
		if(squaredImmunityCircle < 0)
			squaredImmunityCircle = 0;
		else
			squaredImmunityCircle *= squaredImmunityCircle;
		
		threads = new TentacleThread[nbThreads];
		for(int i = 0; i < nbThreads; i++)
		{
			threads[i] = new TentacleThread(config, memorymanager, i, successeurs, buffer);
			if(nbThreads != 1)
				threads[i].start();
		}
		
		courbureMax = config.getDouble(ConfigInfoKraken.MAX_CURVATURE);
	}

	/**
	 * Initialise l'arc manager avec les infos donnée
	 * 
	 * @param directionstrategyactuelle
	 * @param sens
	 * @param arrivee
	 * @throws UnknownModeException 
	 */
	public void configure(DirectionStrategy directionstrategyactuelle, double vitesseMax, Kinematic arrivee, String mode) throws UnknownModeException
	{
		this.vitesseMax = vitesseMax;
		this.directionstrategyactuelle = directionstrategyactuelle;
		currentProfile = profiles.getProfile(mode);
		arrivee.copy(this.arrivee);	
	}
	
	/*
	 * Only used for the reconstruction
	 */
	private LinkedList<EmbodiedKinematic> trajectory = new LinkedList<EmbodiedKinematic>();

	public Map<TentacleType, Integer> getTentaclesStatistics()
	{
		return tentaclesStats;
	}
	
	public void resetTentaclesStatistics()
	{
		tentaclesStats.clear();
	}
	
	public LinkedList<EmbodiedKinematic> reconstruct(AStarNode best, int nbPointsMax)
	{
		trajectory.clear();
		AStarNode noeudParent = best;
		Tentacle arcParent = best.getArc();
		
		EmbodiedKinematic current;
		boolean lastStop = nbPointsMax == Integer.MAX_VALUE; // le dernier point n'est pas un stop en cas de replanification partielle
		boolean nextStop;

		while(noeudParent.parent != null)
		{
			Integer nb = tentaclesStats.get(arcParent.vitesse);
			if(nb == null)
				nb = 0;
			tentaclesStats.put(arcParent.vitesse, nb+1);
			for(int i = arcParent.getNbPoints() - 1; i >= 0; i--)
			{
				current = arcParent.getPoint(i);
				if(printObstacles)
					display.addTemporaryPrintable(current.obstacle.clone(), ColorKraken.ROBOT.color, Layer.BACKGROUND.layer);
				
				nextStop = current.cinem.stop;
				current.cinem.stop = lastStop;
				
				trajectory.addFirst(current);
				
				// stop : on va devoir s'arrêter
				lastStop = nextStop;
			}

			if(nbPointsMax < trajectory.size())
				trajectory.subList(nbPointsMax, trajectory.size()).clear();
			
			noeudParent = noeudParent.parent;
			arcParent = noeudParent.getArc();
		}
		
		return trajectory;
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
//		assert nbLeft == 0; // non, à cause du fast and dirty
		nbLeft = 0;
		int index = 0;

		/*
		 * On-line Largest Processing Time Rule algorithm
		 */
		
		for(TentacleType v : currentProfile.tentacles)
		{
			if(v.isAcceptable(current.cinematique, directionstrategyactuelle, courbureMax))
			{
				nbLeft++;
				assert tasks.size() > index;
				TentacleTask tt = tasks.get(index++);
				tt.arrivee = arrivee;
				tt.current = current;
				tt.v = v;
				tt.computer = v.getComputer();
				tt.vitesseMax = vitesseMax;
				
				if(threads.length == 1) // no multithreading in this case
					threads[0].compute(tt);
				else
					buffer.add(tt);
			}
		}
		
		assert threads.length > 1 || successeurs.size() == nbLeft;
	}

	public synchronized Integer heuristicCostCourbe(Kinematic c)
	{
		Double d = dstarlite.heuristicDistance(c);
		if(d == null)
			return null;
	
		Double o;
		double h;
		if(d < currentProfile.distanceForRealOrientation)
		{
			o = (c.orientationGeometrique - arrivee.orientationReelle) % (2 * Math.PI);
			if(o > Math.PI)
				o -= 2 * Math.PI;
			o = Math.abs(o);
			h = currentProfile.coeffDistanceFinalError * d + currentProfile.coeffFinalAngleError * o;
		}
		else
		{
			if(currentProfile.coeffAngleError != 0)
			{
				o = dstarlite.heuristicOrientation(c);
				if(o == null)
					return null;
				h = currentProfile.coeffDistanceError * d + currentProfile.coeffAngleError * o;
			}
			else
				h = currentProfile.coeffDistanceError * d;
		}
		
		return (int) (1000.*(h / vitesseMax));
	}
	
	public final boolean isNearXYO(Kinematic a, Kinematic b)
	{
		return a.getPosition().squaredDistance(b.getPosition()) - PRECISION_TRACE_MM * PRECISION_TRACE_MM < 1
				&& Math.abs(XYO.angleDifference(a.orientationReelle, b.orientationReelle)) < 0.1;
	}
	
	public final boolean isArrived(Kinematic last)
	{
		return currentProfile.end.isArrived(arrivee, last);
	}

	private AStarNode next;
	
	@Override
	public boolean hasNext()
	{
		assert threads.length > 1 || successeurs.size() == nbLeft : successeurs.size() + " " + nbLeft; // s'il n'y a qu'un seul thread, alors tous les successeurs sont dans la liste
		if(nbLeft == 0)
		{
			assert successeurs.isEmpty();
			return false;
		}
		try {
			do {
				next = successeurs.take();
				nbLeft--;
			} while(nbLeft > 0 && next == TentacleThread.placeholder);
			return next != TentacleThread.placeholder;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
	}

	@Override
	public AStarNode next()
	{
		if(startPosition != null)
		{
			Tentacle arc = next.getArc();
			int nb = arc.getNbPoints();
			for(int i = 0; i < nb; i++)
			{
				EmbodiedKinematic point = arc.getPoint(i);
				if(startPosition.squaredDistance(point.cinem.getPosition()) < squaredImmunityCircle && engine.isThereCollision(point.obstacle))
				{
					point.ignoreCollision = true;
					next.g_score += 100000000;
				}
			}
		}
		return next;
	}

	public void enableStartObstacleImmunity(XY startPosition)
	{
		this.startPosition = startPosition;
	}

}
