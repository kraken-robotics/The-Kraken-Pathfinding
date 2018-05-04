/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import pfg.config.Config;
import pfg.graphic.GraphicDisplay;
import pfg.graphic.printable.Layer;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.LogCategoryKraken;
import pfg.kraken.astar.autoreplanning.DynamicPath;
import pfg.kraken.astar.engine.PhysicsEngine;
import pfg.kraken.astar.tentacles.TentacleManager;
import pfg.kraken.dstarlite.DStarLite;
import pfg.kraken.exceptions.InvalidPathException;
import pfg.kraken.exceptions.NoPathException;
import pfg.kraken.exceptions.NotFastEnoughException;
import pfg.kraken.exceptions.NotInitializedPathfindingException;
import pfg.kraken.exceptions.PathfindingException;
import pfg.kraken.exceptions.TimeoutException;
import pfg.kraken.memory.CinemObsPool;
import pfg.kraken.memory.MemPoolState;
import pfg.kraken.memory.NodePool;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.robot.Cinematique;
import pfg.kraken.robot.CinematiqueObs;
import pfg.kraken.robot.ItineraryPoint;
import pfg.kraken.robot.RobotState;
import pfg.kraken.utils.XY;
import pfg.log.Log;

/**
 * A* qui utilise le D* Lite comme heuristique pour fournir une trajectoire
 * courbe
 * 
 * @author pf
 *
 */

public final class TentacularAStar
{
	protected Log log;
	
	/*
	 * Gives the neighbours
	 */
	private TentacleManager arcmanager;
	
	/*
	 * Heuristic
	 */
	private DStarLite dstarlite;
	
	/*
	 * The memory pool of AStarNode
	 */
	private NodePool memorymanager;
	
	/*
	 * Graphic display
	 */
	private GraphicDisplay buffer;
	
	private PhysicsEngine engine;
	
	/*
	 * The departure node
	 */
	private AStarNode depart;
	
	private Cinematique arrival = new Cinematique();
	
	/*
	 * The last node of a path that has been found (but better routes are expected)
	 */
	private AStarNode trajetDeSecours;
	
	/*
	 * Memory pool of obstacles
	 */
	private CinemObsPool cinemMemory;
	
	/*
	 * Just a path container
	 */
	private DynamicPath chemin;
	
	/*
	 * Graphic parameters
	 */
	private boolean graphicTrajectory;

	/*
	 * Duration before timeout
	 */
	private int dureeMaxPF;
	
	/*
	 * The default direction strategy
	 */
	private DirectionStrategy defaultStrategy;
	
	/*
	 * The default max speed
	 */
	private double defaultSpeed;
	
	/*
	 * Some debug variables
	 */
	private int nbExpandedNodes;
	private boolean debugMode;
	private boolean initialized = false;
	private boolean checkEachIteration;
	
	/**
	 * Comparateur de noeud utilisé par la priority queue.
	 * 
	 * @author pf
	 *
	 */
	private class AStarCourbeNodeComparator implements Comparator<AStarNode>
	{
		@Override
		public final int compare(AStarNode arg0, AStarNode arg1)
		{
			// Ordre lexico : on compare d'abord first, puis second
			int tmp = (arg0.f_score - arg1.f_score);
			if(tmp != 0)
				return tmp;
			return arg0.g_score - arg1.g_score;
		}
	}

	/*
	 * The set of processed nodes
	 */
	private final HashSet<Integer> closedset = new HashSet<Integer>();
	
	/*
	 * The set of nodes that need to be processed
	 */
	private final PriorityQueue<AStarNode> openset = new PriorityQueue<AStarNode>(5000, new AStarCourbeNodeComparator());
	
	/*
	 * Only used for the reconstruction
	 */
//	private Stack<Tentacle> pileTmp = new Stack<Tentacle>();
	
	/*
	 * For graphical display purpose only
	 */
	private List<AStarNode> outTentacles = new ArrayList<AStarNode>();
	
	private RectangularObstacle vehicleTemplate;
	
	private boolean fastMode;
	
	/**
	 * Constructeur du AStarCourbe
	 */
	public TentacularAStar(Log log, PhysicsEngine engine, DynamicPath defaultChemin, DStarLite dstarlite, TentacleManager arcmanager, NodePool memorymanager, CinemObsPool rectMemory, GraphicDisplay buffer, RobotState chrono, Config config, RectangularObstacle vehicleTemplate)
	{
		this.engine = engine;
		this.chemin = defaultChemin;
		this.log = log;
		this.arcmanager = arcmanager;
		this.memorymanager = memorymanager;
		this.dstarlite = dstarlite;
		this.cinemMemory = rectMemory;
		this.buffer = buffer;
		graphicTrajectory = config.getBoolean(ConfigInfoKraken.GRAPHIC_TENTACLES);
		debugMode = config.getBoolean(ConfigInfoKraken.ENABLE_DEBUG_MODE);
		fastMode = config.getBoolean(ConfigInfoKraken.FAST_AND_DIRTY);
		checkEachIteration = config.getBoolean(ConfigInfoKraken.CHECK_NEW_OBSTACLES);
		if(debugMode)
			dureeMaxPF = Integer.MAX_VALUE;
		else
			dureeMaxPF = config.getInt(ConfigInfoKraken.SEARCH_TIMEOUT);

		if(config.getBoolean(ConfigInfoKraken.ALLOW_BACKWARD_MOTION))
			defaultStrategy = DirectionStrategy.FASTEST;
		else
			defaultStrategy = DirectionStrategy.FORCE_FORWARD_MOTION;
		defaultSpeed = config.getDouble(ConfigInfoKraken.DEFAULT_MAX_SPEED);
		this.depart = new AStarNode(chrono, vehicleTemplate);
		depart.setIndiceMemoryManager(-1);
		this.vehicleTemplate = vehicleTemplate;
	}

	public List<ItineraryPoint> searchWithoutReplanning() throws PathfindingException
	{
		chemin.initSearchWithoutPlanning();
		log.write("Path search begins.", LogCategoryKraken.PF);
		search();
		return chemin.endSearchWithoutPlanning();
	}
	
	private Cinematique tmp = new Cinematique();
	private List<RectangularObstacle> initialObstacles = new ArrayList<RectangularObstacle>();

	public void checkAfterInitialization(List<ItineraryPoint> initialPath) throws InvalidPathException
	{
		tmp.update(initialPath.get(0));
		if(!arcmanager.isNearXYO(depart.robot.getCinematique(), tmp))
			throw new InvalidPathException("The first point doesn't match the start.");
		
		tmp.update(initialPath.get(initialPath.size() - 1));
		if(!arcmanager.isArrived(tmp))
			throw new InvalidPathException("The final point doesn't match the finish.");
		
		initialObstacles.clear();
		for(ItineraryPoint ip : initialPath)
		{
			RectangularObstacle o = vehicleTemplate.clone();
			o.update(new XY(ip.x, ip.y), ip.orientation);
			initialObstacles.add(o);
		}
		
		if(engine.isThereCollision(initialObstacles))
			throw new InvalidPathException("There are obstacles through the path.");
	}
	
	public void searchWithReplanningAndInitialPath(List<ItineraryPoint> initialPath) throws PathfindingException
	{
		checkAfterInitialization(initialPath);
		
		chemin.importPath(initialPath);
		log.write("Path search with autoreplanning begins.", LogCategoryKraken.PF);
	}
	
	public void searchWithReplanning() throws PathfindingException
	{
		chemin.setSearchInProgress();
		log.write("Path search with autoreplanning begins.", LogCategoryKraken.PF);
		search();
	}
	
	/**
	 * Le calcul du AStarCourbe
	 * 
	 * @param depart
	 * @return
	 * @throws PathfindingException
	 * @throws MemoryPoolException
	 */
	private final synchronized void search() throws PathfindingException
	{
		if(!initialized)
			throw new NotInitializedPathfindingException("Search not initialized !");

		trajetDeSecours = null;
		depart.parent = null;
		depart.cameFromArcDynamique = null;
		depart.g_score = 0;
		nbExpandedNodes = 0;
		
		Integer heuristique = arcmanager.heuristicCostCourbe((depart.robot).getCinematique());

		assert heuristique != null : "Null heuristic !"; // l'heuristique est vérifiée à l'initialisation

		depart.f_score = heuristique;
		openset.clear();
		assert setState(depart, MemPoolState.WAITING);
		openset.add(depart); // Les nœuds à évaluer
		closedset.clear();

		long debutRecherche = System.currentTimeMillis();

		AStarNode current;
		do
		{
			current = openset.poll();
			if(checkEachIteration)
			{
				engine.update();
				dstarlite.updateObstacles();
			}

			assert current.parent != null || current == depart;
			assert current == depart || current.parent == depart || current.parent.getArc() != null : current == depart ? "Départ" : current.parent.getArc();
			assert current.parent == null || current.parent.getState() == MemPoolState.STANDBY;
			assert current.getState() == MemPoolState.WAITING && setState(current, MemPoolState.CURRENT) : current.getState();
			
			nbExpandedNodes++;
			
			if(chemin.isModeWithReplanning() && !chemin.isInitialSearch())
			{
				synchronized(chemin)
				{
					// Doit-on s'arrêter ?
					chemin.checkException();
		
					int margeDemandee = chemin.margeSupplementaireDemandee();
					// On vérifie régulièrement qu'il ne faut pas fournir un chemin partiel
					if(margeDemandee > 0)
					{
						log.write("Partial rebuild required !", LogCategoryKraken.PF);
						partialReconstruct(current, chemin, margeDemandee, true);
						
						if(chemin.margeSupplementaireDemandee() > 0) // toujours pas assez de marge : on doit arrêter
							throw new NotFastEnoughException("Not enough margin.");
		
						depart.robot.setCinematique(chemin.getNewStart());
						trajetDeSecours = null;
						depart.parent = null;
						depart.cameFromArcDynamique = null;
						depart.g_score = 0;
						heuristique = arcmanager.heuristicCostCourbe((depart.robot).getCinematique());
		
						if(heuristique == null)
							throw new NoPathException("No path found by the D* Lite");
		
						depart.f_score = heuristique;
		
						memorymanager.empty();
						cinemMemory.empty();
						closedset.clear();
						openset.clear();
						
						debutRecherche = System.currentTimeMillis();
						current = depart;
					}
				}
			}

			// si on a déjà fait ce point ou un point très proche…
			// exception si c'est un point d'arrivée
			if(!closedset.add(current.hashCode()) && (current.getArc() == null || !arcmanager.isArrived(current.getArc().getLast())))
			{
				// we skip this point
				if(current != depart)
				{
					memorymanager.destroyNode(current);
					assert current.getState() == MemPoolState.FREE;
				}
				continue;
			}

			// ce calcul étant un peu lourd, on ne le fait que si le noeud a été
			// choisi, et pas à la sélection des voisins (dans hasNext par
			// exemple) (expérimentalement vérifié sur pc et raspi)
			if(current.parent != null && engine.isThereCollision(current.getArc()))
			{
				assert current != depart;
				if(current != depart)
				{
					memorymanager.destroyNode(current);
					assert current.getState() == MemPoolState.FREE;
				}
				continue; // collision mécanique attendue. On passe au suivant !
			}

			// affichage
			if(graphicTrajectory && current.getArc() != null)
			{
				buffer.addTemporaryPrintable(current, current.getArc().vitesse.getColor(), Layer.MIDDLE.layer);
				if(current.parent != null)
				{
					buffer.addTemporaryPrintable(current.parent, Color.ORANGE, Layer.FOREGROUND.layer);
					assert current.parent == depart || current.parent.getArc().getNbPoints() > 0;
				}
			}

			// Si current est la trajectoire de secours, ça veut dire que cette
			// trajectoire de secours est la meilleure possible, donc on a fini
			if(current == trajetDeSecours)
			{
				partialReconstruct(current, chemin, Integer.MAX_VALUE, false);
				memorymanager.empty();
				cinemMemory.empty();
				return;
			}

			long elapsed = System.currentTimeMillis() - debutRecherche;

/*			if(!rechercheEnCours)
			{
				chemin.setUptodate();
				log.write("The path search has been canceled.", SeverityCategoryKraken.WARNING, LogCategoryKraken.PF);
				return;
			}*/
			
			if(elapsed > dureeMaxPF)
			{
				/*
				 * Timeout !
				 */

				memorymanager.empty();
				cinemMemory.empty();
				if(trajetDeSecours != null) // si on a un trajet de secours, on l'utilise
				{
					log.write("The backup path is used.", LogCategoryKraken.PF);
					partialReconstruct(trajetDeSecours, chemin, Integer.MAX_VALUE, false);
					return;
				}
				
				// sinon, on lève une exception
				throw new TimeoutException("Timeout pathfinding !");
			}

			// On parcourt les voisins de current
			arcmanager.computeTentacles(current);
			if(debugMode)
				outTentacles.clear();

			while(arcmanager.hasNext())
			{
				AStarNode successeur = arcmanager.next();
				assert successeur.getArc().getNbPoints() > 0;
				
				// Il y a une trop grande distance
				if(successeur.getArc().getPoint(0).getPosition().distanceFast(current.robot.getCinematique().getPosition()) > 35)
				{
					assert false : "Distance entre deux points trop élevée : " + successeur.getArc()+" "+current.robot.getCinematique().getPosition()+" "+successeur.getArc().getPoint(0).getPosition().distanceFast(current.robot.getCinematique().getPosition());
					memorymanager.destroyNode(successeur);
					continue;
				}
				assert successeur.getState() == MemPoolState.NEXT : successeur.getState();
				successeur.g_score += current.g_score; // successeur.g_score contient déjà la distance entre current et successeur

				// on a déjà visité un point proche?
				// ceci est vraie seulement si l'heuristique est monotone. C'est
				// normalement le cas.
				if(closedset.contains(successeur.hashCode()))
				{
					memorymanager.destroyNode(successeur);
					continue;
				}

				heuristique = arcmanager.heuristicCostCourbe(successeur.robot.getCinematique());
				if(heuristique == null)
				{
					// Point inaccessible
					memorymanager.destroyNode(successeur);
					continue;
				}

				successeur.f_score = successeur.g_score + heuristique;

				// est qu'on est tombé sur l'arrivée ? alors ça fait un trajet de secours
				// s'il y a déjà un trajet de secours, on prend le meilleur
				if(successeur.getArc() != null && arcmanager.isArrived(successeur.getArc().getLast()) && (successeur.getArc() == null || !engine.isThereCollision(successeur.getArc())) && (trajetDeSecours == null || trajetDeSecours.f_score > successeur.f_score))
				{
					trajetDeSecours = successeur;
					if(fastMode)
					{
						log.write("A fast-and-dirty path is used.", LogCategoryKraken.PF);
						partialReconstruct(trajetDeSecours, chemin, Integer.MAX_VALUE, false);
						memorymanager.empty();
						cinemMemory.empty();
						return;
					}
				}
					/*
					 * Cela ne sert à rien de détruire l'ancien trajet de secours (qui est dans l'openset, car si on l'avait pioché de l'openset on aurait fini avec lui)
					 * C'est juste qu'on garde le meilleur dans un coin.
					 */
				
				if(debugMode)
				{
					outTentacles.add(successeur);
					buffer.addTemporaryPrintable(successeur, Color.BLUE, Layer.FOREGROUND.layer);
				}

				openset.add(successeur);
				assert setState(successeur, MemPoolState.WAITING);
			}
			
			if(debugMode)
			{
				buffer.refresh();
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				for(AStarNode n : outTentacles)
					buffer.removePrintable(n);
				buffer.refresh();
			}

			assert setState(current, MemPoolState.STANDBY);
		} while(!openset.isEmpty());

		/**
		 * Plus aucun nœud à explorer
		 */
		memorymanager.empty();
		cinemMemory.empty();
		throw new NoPathException("All the space has been searched and no path has been found ("+nbExpandedNodes+" expanded nodes)");
	}
	
	/**
	 * Reconstruit le chemin. Il peut reconstruire le chemin même si celui-ci
	 * n'est pas fini.
	 * 
	 * @param best
	 * @param last
	 * @throws PathfindingException
	 */
	private final void partialReconstruct(AStarNode best, DynamicPath chemin, int nbPointsMax, boolean partial)
	{
		if(debugMode)
		{
			System.out.println("Path duration : "+best.robot.getDate());
			System.out.println("Number of expanded nodes : "+nbExpandedNodes);
		}

		LinkedList<CinematiqueObs> trajectory = arcmanager.reconstruct(best, nbPointsMax);
		
		assert trajectory.size() <= nbPointsMax : trajectory.size()+" "+nbPointsMax;
		chemin.addToEnd(trajectory, partial);
		
		
		log.write("Research completed.", LogCategoryKraken.PF);
	}
	
	/**
	 * Calcul de chemin classique
	 * 
	 * @param arrivee
	 * @param sens
	 * @param shoot
	 * @throws NoPathException 
	 */
	public void initializeNewSearch(Cinematique start, Cinematique arrival, DirectionStrategy directionstrategy, String mode, Double maxSpeed) throws NoPathException
	{
		initialized = true;
		depart.init();
		depart.robot.setCinematique(start);
		arrival.copy(this.arrival);
		arcmanager.configure(directionstrategy == null ? defaultStrategy : directionstrategy, maxSpeed == null ? defaultSpeed : maxSpeed, arrival, mode);
		engine.update();
		
		/*
		 * dstarlite.computeNewPath updates the heuristic.
		 * It returns false if there is no path between start and arrival
		 */
		if(!dstarlite.computeNewPath(depart.robot.getCinematique().getPosition(), arrival.getPosition()))
			throw new NoPathException("No path found by D* Lite !");
	}
	
	/**
	 * Replanification. On conserve la même DirectionStrategy ainsi que le même
	 * SensFinal
	 * Par contre, si besoin est, on peut changer la politique de shootage
	 * d'éléments de jeu
	 * S'il n'y avait aucun recherche en cours, on ignore.
	 * 
	 * @param shoot
	 * @throws PathfindingException
	 * @throws InterruptedException
	 */
	public void updatePath(Cinematique lastValid) throws PathfindingException
	{
		assert chemin.needReplanning();
		if(!chemin.needReplanning())
			return;
		
		log.write("Replanning started.", LogCategoryKraken.REPLANIF);

		depart.init();
		depart.robot.setCinematique(lastValid);

		// On met à jour le D* Lite
		engine.update();
		
		if(!dstarlite.computeNewPath(depart.robot.getCinematique().getPosition(), arrival.getPosition()))
			throw new NoPathException("No path found by D* Lite !");

		search();
	}
	
/*	private String checkOpenSet()
	{
		for(AStarNode n : openset)
			if(n.getState() != MemPoolState.WAITING)
				return n.getIndiceMemoryManager()+" "+n.getState();
		return null;
	}*/
	
	private boolean setState(AStarNode node, MemPoolState state)
	{
		node.setState(state);
		return true;
	}
}