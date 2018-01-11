/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
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
import pfg.kraken.astar.tentacles.TentacleManager;
import pfg.kraken.dstarlite.DStarLite;
import pfg.kraken.exceptions.NoPathException;
import pfg.kraken.exceptions.PathfindingException;
import pfg.kraken.exceptions.TimeoutException;
import pfg.kraken.memory.CinemObsPool;
import pfg.kraken.memory.MemPoolState;
import pfg.kraken.memory.NodePool;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.robot.Cinematique;
import pfg.kraken.robot.ItineraryPoint;
import pfg.kraken.robot.RobotState;
import pfg.graphic.log.Log;
import pfg.kraken.utils.XY;
import pfg.kraken.utils.XYO;

/**
 * A* qui utilise le D* Lite comme heuristique pour fournir une trajectoire
 * courbe
 * 
 * @author pf
 *
 */

public class TentacularAStar
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
	
	/*
	 * The departure node
	 */
	private AStarNode depart;
	
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
	private DefaultCheminPathfinding defaultChemin;
	
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
	
	/*
	 * Check if a search ongoing FIXME unused
	 */
//	private volatile boolean rechercheEnCours = false;

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
	 * Only used for the reconstruction
	 */
	private LinkedList<ItineraryPoint> trajectory = new LinkedList<ItineraryPoint>();
	
	/*
	 * For graphical display purpose only
	 */
	private List<AStarNode> outTentacles = new ArrayList<AStarNode>();
	
	/**
	 * Constructeur du AStarCourbe
	 */
	public TentacularAStar(Log log, DefaultCheminPathfinding defaultChemin, DStarLite dstarlite, TentacleManager arcmanager, NodePool memorymanager, CinemObsPool rectMemory, GraphicDisplay buffer, RobotState chrono, Config config, RectangularObstacle vehicleTemplate)
	{
		this.defaultChemin = defaultChemin;
		this.log = log;
		this.arcmanager = arcmanager;
		this.memorymanager = memorymanager;
		this.dstarlite = dstarlite;
		this.cinemMemory = rectMemory;
		this.buffer = buffer;
		graphicTrajectory = config.getBoolean(ConfigInfoKraken.GRAPHIC_TENTACLES);
		debugMode = config.getBoolean(ConfigInfoKraken.ENABLE_DEBUG_MODE);
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
	}

	public LinkedList<ItineraryPoint> search() throws PathfindingException
	{
		search(defaultChemin, false);
		return defaultChemin.getPath();
	}
	
	/**
	 * Le calcul du AStarCourbe
	 * 
	 * @param depart
	 * @return
	 * @throws PathfindingException
	 * @throws InterruptedException
	 * @throws MemoryPoolException
	 */
	private final synchronized void search(CheminPathfindingInterface chemin, boolean replanif) throws PathfindingException
	{
//		if(!rechercheEnCours)
//			throw new NotInitializedPathfindingException("updatePath appelé alors qu'aucune recherche n'est en cours !");

		log.write("Path search begins.", LogCategoryKraken.PF);
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

			assert current.parent != null || current == depart;
			assert current == depart || current.parent == depart || current.parent.getArc() != null : current == depart ? "Départ" : current.parent.getArc();
			assert current.parent == null || current.parent.getState() == MemPoolState.STANDBY;
			assert current.getState() == MemPoolState.WAITING && setState(current, MemPoolState.CURRENT) : current.getState();
			
			nbExpandedNodes++;
			
			// FIXME : this part is currently disable
/*			if(replanif && chemin.needStop())
				throw new NotFastEnoughException("Obstacle seen too late, there is not enough margin.");

			// On vérifie régulièremet s'il ne faut pas fournir un chemin
			// partiel
			Cinematique cinemRestart = chemin.getLastValidCinematique();
			boolean assezDeMarge = chemin.aAssezDeMarge();

			if(cinemRestart != null || !assezDeMarge)
			{
				if(!assezDeMarge)
				{
					log.write("Partial rebuild required !", LogCategoryKraken.PF);
					depart.robot.setCinematique(partialReconstruct(current, chemin, 2));
					if(!chemin.aAssezDeMarge()) // toujours pas assez de marge
												// : on doit arrêter
						throw new NotFastEnoughException("Not enough margin.");
				}
				else // il faut partir d'un autre point
				{
					log.write("The search is restarted from " + cinemRestart, LogCategoryKraken.REPLANIF);
					depart.init();
					depart.robot.setCinematique(cinemRestart);
				}

				trajetDeSecours = null;
				depart.parent = null;
				depart.cameFromArcDynamique = null;
				depart.g_score = 0;
				heuristique = arcmanager.heuristicCostCourbe((depart.robot).getCinematique());

				if(heuristique == null)
					throw new NoPathException("No path found by the D* Lite");

				depart.f_score = heuristique / vitesseMax;

				memorymanager.empty();
				cinemMemory.empty();
				closedset.clear();
				openset.clear();

				current = depart;
			}*/

			// si on a déjà fait ce point ou un point très proche…
			// exception si c'est un point d'arrivée
			if(!closedset.add(current.hashCode()) && !arcmanager.isArrived(current))
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
			// exemple)
			if(!arcmanager.isReachable(current))
			{
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
				chemin.setUptodate();
				partialReconstruct(current, chemin);
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
			
			if(!replanif && elapsed > dureeMaxPF)
			{
				/*
				 * Timeout !
				 */

				memorymanager.empty();
				cinemMemory.empty();
				if(trajetDeSecours != null) // si on a un trajet de secours, on l'utilise
				{
					log.write("The backup path is used.", LogCategoryKraken.PF);
					chemin.setUptodate();
					partialReconstruct(trajetDeSecours, chemin);
					return;
				}
				
				// sinon, on lève une exception
				throw new TimeoutException("Pathfinding timeout !");
			}

			// On parcourt les voisins de current
			arcmanager.computeTentacles(current);
			if(debugMode)
				outTentacles.clear();

			while(arcmanager.hasNext())
			{
				AStarNode successeur = arcmanager.next();
				assert successeur.getArc().getNbPoints() > 0;
				assert successeur.getArc().getPoint(0).getPosition().distanceFast(current.robot.getCinematique().getPosition()) < 35 : successeur.getArc()+" "+current.robot.getCinematique().getPosition()+" "+successeur.getArc().getPoint(0).getPosition().distanceFast(current.robot.getCinematique().getPosition());
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
				// TODO
				if(arcmanager.isArrived(successeur) && arcmanager.isReachable(successeur) && (trajetDeSecours == null || trajetDeSecours.f_score > successeur.f_score))
				{
					// on détruit l'ancien trajet
					if(trajetDeSecours != null)
					{
						assert trajetDeSecours.getState() == MemPoolState.STANDBY && setState(trajetDeSecours, MemPoolState.CURRENT);
						memorymanager.destroyNode(trajetDeSecours);
					}

					trajetDeSecours = successeur;
					assert setState(trajetDeSecours, MemPoolState.STANDBY);
					continue;
				}
				
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
					e.printStackTrace();
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
	private final void partialReconstruct(AStarNode best, CheminPathfindingInterface chemin)
	{
		if(debugMode)
		{
			System.out.println("Path duration : "+best.robot.getDate());
			System.out.println("Number of expanded nodes : "+nbExpandedNodes);
		}

		trajectory.clear();

		arcmanager.reconstruct(trajectory, best);
		
		chemin.addToEnd(trajectory);
		log.write("Research completed.", LogCategoryKraken.PF);

//		return last;
	}
	
	public void initializeNewSearch(XYO start, XY arrival) throws NoPathException
	{
		initializeNewSearch(start, arrival, defaultStrategy);
	}

	public void initializeNewSearch(XYO start, XY arrival, DirectionStrategy directionstrategy) throws NoPathException
	{
		initializeNewSearch(start, arrival, directionstrategy, defaultSpeed);
	}
	
	/**
	 * Calcul de chemin classique
	 * 
	 * @param arrivee
	 * @param sens
	 * @param shoot
	 * @throws NoPathException 
	 */
	public void initializeNewSearch(XYO start, XY arrival, DirectionStrategy directionstrategy, double maxSpeed) throws NoPathException
	{
		depart.init();
		depart.robot.setCinematique(new Cinematique(start));
		arcmanager.configure(directionstrategy, maxSpeed, arrival);

		/*
		 * dstarlite.computeNewPath updates the heuristic.
		 * It returns false if there is no path between start and arrival
		 */
		if(!dstarlite.computeNewPath(depart.robot.getCinematique().getPosition(), arrival))
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
/*	protected void updatePath(Cinematique lastValid) throws NotInitializedPathfindingException
	{
		// TODO : disabled
		if(!rechercheEnCours)
			throw new NotInitializedPathfindingException("updatePath is called before the initialization !");

		log.write("Replanification lancée", LogCategoryKraken.REPLANIF);

		depart.init();
// TODO		state.copyAStarCourbe(depart.state);
*/
		/*
		 * Forcément, on utilise le vrai chemin ici
		 */
/*
		closedset.clear();
		depart.robot.setCinematique(lastValid);
*/
/*		if(suppObsFixes)
		{
			CinematiqueObs obsDepart = cinemMemory.getNewNode();
			Cinematique cinemDepart = depart.state.robot.getCinematique();
			obsDepart.updateReel(cinemDepart.getPosition().getX(), cinemDepart.getPosition().getY(), cinemDepart.orientationReelle, cinemDepart.enMarcheAvant, cinemDepart.courbureReelle);
			arcmanager.disableObstaclesFixes(symetrie, obsDepart);
		}*/
/*
		// On met à jour le D* Lite
		dstarlite.updateObstacles();

//		process(realChemin, true);
	}*/
	
	private boolean setState(AStarNode node, MemPoolState state)
	{
		node.setState(state);
		return true;
	}
}