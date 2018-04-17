/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar.tentacles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import pfg.config.Config;
import pfg.injector.Injector;
import pfg.injector.InjectorException;
import pfg.kraken.ColorKraken;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.astar.AStarNode;
import pfg.kraken.astar.DirectionStrategy;
import pfg.kraken.astar.tentacles.endCheck.EndWithXYO;
import pfg.kraken.astar.tentacles.types.TentacleType;
import pfg.kraken.astar.thread.TentacleTask;
import pfg.kraken.astar.thread.TentacleThread;
import pfg.kraken.dstarlite.DStarLite;
import pfg.kraken.exceptions.UnknownModeException;
import pfg.kraken.memory.NodePool;
import pfg.kraken.robot.Cinematique;
import pfg.kraken.robot.CinematiqueObs;
import pfg.kraken.utils.XYO;
import pfg.graphic.GraphicDisplay;
import pfg.log.Log;
import pfg.graphic.printable.Layer;
import static pfg.kraken.astar.tentacles.Tentacle.*;

/**
 * Réalise des calculs pour l'A* courbe.
 * 
 * @author pf
 *
 */

public final class TentacleManager implements Iterator<AStarNode>
{
	protected Log log;
	private DStarLite dstarlite;
	private double courbureMax, maxLinearAcceleration, vitesseMax;
	private boolean printObstacles;
	private Injector injector;
	private double deltaSpeedFromStop;
	private GraphicDisplay display;
	private TentacleThread[] threads;
	private ResearchProfile currentProfile;
	private EndWithXYO endXYOCheck = new EndWithXYO();
	
	private DirectionStrategy directionstrategyactuelle;
	private Cinematique arrivee = new Cinematique();
	private ResearchProfileManager profiles;
	private List<TentacleTask> tasks = new ArrayList<TentacleTask>();
	private BlockingQueue<AStarNode> successeurs = new LinkedBlockingQueue<AStarNode>();
	private BlockingQueue<TentacleTask> buffer = new LinkedBlockingQueue<TentacleTask>();
	
	private int nbLeft;
	
	public TentacleManager(Log log, NodePool memorymanager, DStarLite dstarlite, Config config, Injector injector, ResearchProfileManager profiles, GraphicDisplay display) throws InjectorException
	{
		this.injector = injector;
		this.log = log;
		this.dstarlite = dstarlite;
		this.display = display;
		this.profiles = profiles;
		
		for(int i = 0; i < 100; i++)
			tasks.add(new TentacleTask());
		
		maxLinearAcceleration = config.getDouble(ConfigInfoKraken.MAX_LINEAR_ACCELERATION);
		deltaSpeedFromStop = Math.sqrt(2 * PRECISION_TRACE * maxLinearAcceleration);

		printObstacles = config.getBoolean(ConfigInfoKraken.GRAPHIC_ROBOT_COLLISION);
		int nbThreads = config.getInt(ConfigInfoKraken.THREAD_NUMBER);
		
		threads = new TentacleThread[nbThreads];
		for(int i = 0; i < nbThreads; i++)
		{
			threads[i] = new TentacleThread(log, config, memorymanager, i, successeurs, buffer);
			if(nbThreads != 1)
				threads[i].start();
		}
		
		courbureMax = config.getDouble(ConfigInfoKraken.MAX_CURVATURE);
	}

	public void updateProfiles(ResearchProfile mode)
	{
		try {
			List<TentacleType> profile = mode.tentacles;
			for(TentacleType t : profile)
				injector.getService(t.getComputer());
		} catch(InjectorException e)
		{
			e.printStackTrace();
		}
	}
	

	/**
	 * Initialise l'arc manager avec les infos donnée
	 * 
	 * @param directionstrategyactuelle
	 * @param sens
	 * @param arrivee
	 * @throws UnknownModeException 
	 */
	public void configure(DirectionStrategy directionstrategyactuelle, double vitesseMax, Cinematique arrivee, String mode) throws UnknownModeException
	{
		this.vitesseMax = vitesseMax;
		this.directionstrategyactuelle = directionstrategyactuelle;
		currentProfile = profiles.getProfile(mode);
		arrivee.copy(this.arrivee);	
	}
	
	/*
	 * Only used for the reconstruction
	 */
	private LinkedList<CinematiqueObs> trajectory = new LinkedList<CinematiqueObs>();

	public LinkedList<CinematiqueObs> reconstruct(AStarNode best, int nbPointsMax)
	{
		trajectory.clear();
		AStarNode noeudParent = best;
		Tentacle arcParent = best.getArc();
		
		CinematiqueObs current;
		boolean lastStop = nbPointsMax == Integer.MAX_VALUE; // le dernier point n'est pas un stop en cas de replanification partielle
		boolean nextStop;
		double lastPossibleSpeed = 0;

		while(noeudParent.parent != null)
		{
			for(int i = arcParent.getNbPoints() - 1; i >= 0; i--)
			{
				current = arcParent.getPoint(i);
				if(printObstacles)
					display.addTemporaryPrintable(current.obstacle.clone(), ColorKraken.ROBOT.color, Layer.BACKGROUND.layer);
				
				// vitesse maximale du robot à ce point
				double maxSpeed = current.possibleSpeed;
				double currentSpeed = lastPossibleSpeed;
				
				nextStop = current.stop;
				if(lastStop)
					current.possibleSpeed = 0;
				else if(currentSpeed < maxSpeed)
				{
					double deltaVitesse;
					if(currentSpeed < 0.1)
						deltaVitesse = deltaSpeedFromStop;
					else
						deltaVitesse = 2 * maxLinearAcceleration * PRECISION_TRACE / currentSpeed;

					currentSpeed += deltaVitesse;
					currentSpeed = Math.min(currentSpeed, maxSpeed);
					current.possibleSpeed = currentSpeed;
				}
				current.stop = lastStop;
				
				trajectory.addFirst(current);
				
				// stop : on va devoir s'arrêter
				lastPossibleSpeed = current.possibleSpeed;
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
		int index = 0;
		assert nbLeft == 0;

		/*
		 * On-line Largest Processing Time Rule algorithm
		 */
		
		for(TentacleType v : currentProfile.tentacles)
		{
			if(v.isAcceptable(current.robot.getCinematique(), directionstrategyactuelle, courbureMax))
			{
				nbLeft++;
				assert tasks.size() > index;
				TentacleTask tt = tasks.get(index++);
				tt.arrivee = arrivee;
				tt.current = current;
				tt.v = v;
				tt.computer = injector.getExistingService(v.getComputer());
				tt.vitesseMax = vitesseMax;
				
				if(threads.length == 1) // no multithreading in this case
					threads[0].compute(tt);
				else
					buffer.add(tt);
			}
		}
	}

	public synchronized Integer heuristicCostCourbe(Cinematique c)
	{
		Double h = dstarlite.heuristicCostCourbe(c, currentProfile.coeffDistanceError, currentProfile.coeffAngleError);
		if(h == null)
			return null;
		if(currentProfile.coeffFinalAngleError > 0 && c.getPosition().distanceFast(arrivee.getPosition()) < 100)
		{
			h += currentProfile.coeffFinalAngleError*Math.abs(XYO.angleDifference(c.orientationReelle, arrivee.orientationReelle));
		}
		return (int) (1000.*(h / vitesseMax));
	}
	
	public final boolean isNearXYO(Cinematique a, Cinematique b)
	{
		return endXYOCheck.isArrived(a, b);
	}
	
	public final boolean isArrived(Cinematique last)
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
		return next;
	}
}
