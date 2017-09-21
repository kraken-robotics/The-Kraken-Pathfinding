/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.astar;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Stack;
import pfg.config.Config;
import pfg.graphic.PrintBuffer;
import pfg.kraken.ConfigInfoKraken;
import pfg.kraken.LogCategoryKraken;
import pfg.kraken.SeverityCategoryKraken;
import pfg.kraken.astar.tentacles.Tentacle;
import pfg.kraken.astar.tentacles.TentacleManager;
import pfg.kraken.dstarlite.DStarLite;
import pfg.kraken.exceptions.NoPathException;
import pfg.kraken.exceptions.NotFastEnoughException;
import pfg.kraken.exceptions.NotInitializedPathfindingException;
import pfg.kraken.exceptions.PathfindingException;
import pfg.kraken.exceptions.TimeoutException;
import pfg.kraken.memory.CinemObsPool;
import pfg.kraken.memory.NodePool;
import pfg.kraken.robot.Cinematique;
import pfg.kraken.robot.DefaultSpeed;
import pfg.kraken.robot.ItineraryPoint;
import pfg.kraken.robot.RobotState;
import pfg.kraken.robot.KrakenSpeed;
import pfg.log.Log;
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
	private TentacleManager arcmanager;
	private DStarLite dstarlite;
	private NodePool memorymanager;
	private PrintBuffer buffer;
	private AStarNode depart;
	private AStarNode trajetDeSecours;
	private CinemObsPool cinemMemory;
	private DefaultCheminPathfinding defaultChemin;
	private boolean graphicTrajectory, graphicDStarLite, graphicTrajectoryAll;
	private int dureeMaxPF;
	private KrakenSpeed vitesseMax;
	// private int tailleFaisceau;
	private volatile boolean rechercheEnCours = false;

	/**
	 * Comparateur de noeud utilisé par la priority queue
	 * 
	 * @author pf
	 *
	 */
	private class AStarCourbeNodeComparator implements Comparator<AStarNode>
	{
		@Override
		public int compare(AStarNode arg0, AStarNode arg1)
		{
			// Ordre lexico : on compare d'abord first, puis second
			int tmp = (int) (arg0.f_score - arg1.f_score);
			if(tmp != 0)
				return tmp;
			return (int) (arg0.g_score - arg1.g_score);
		}
	}

	private final HashSet<AStarNode> closedset = new HashSet<AStarNode>();
	private final PriorityQueue<AStarNode> openset = new PriorityQueue<AStarNode>(5000, new AStarCourbeNodeComparator());
	private Stack<Tentacle> pileTmp = new Stack<Tentacle>();
	private LinkedList<ItineraryPoint> trajectory = new LinkedList<ItineraryPoint>();

	// private HashSet<AStarCourbeNode> closedsetTmp = new
	// HashSet<AStarCourbeNode>();
	// private final PriorityQueue<AStarCourbeNode> opensetTmp = new
	// PriorityQueue<AStarCourbeNode>(100, new AStarCourbeNodeComparator());

	/**
	 * Constructeur du AStarCourbe
	 */
	public TentacularAStar(Log log, DefaultCheminPathfinding defaultChemin, DStarLite dstarlite, TentacleManager arcmanager, NodePool memorymanager, CinemObsPool rectMemory, PrintBuffer buffer, RobotState chrono, Config config)
	{
		this.defaultChemin = defaultChemin;
		this.log = log;
		this.arcmanager = arcmanager;
		this.memorymanager = memorymanager;
		this.dstarlite = dstarlite;
		this.cinemMemory = rectMemory;
		this.buffer = buffer;
		graphicTrajectory = config.getBoolean(ConfigInfoKraken.GRAPHIC_TRAJECTORY);
		graphicTrajectoryAll = config.getBoolean(ConfigInfoKraken.GRAPHIC_TRAJECTORY_ALL);
//		graphicDStarLite = config.getBoolean(ConfigInfoKraken.GRAPHIC_D_STAR_LITE);
		dureeMaxPF = config.getInt(ConfigInfoKraken.DUREE_MAX_RECHERCHE_PF);
		// tailleFaisceau = config.getInt(ConfigInfo.TAILLE_FAISCEAU_PF);
		int demieLargeurNonDeploye = config.getInt(ConfigInfoKraken.LARGEUR_NON_DEPLOYE) / 2;
		int demieLongueurArriere = config.getInt(ConfigInfoKraken.DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE);
		int demieLongueurAvant = config.getInt(ConfigInfoKraken.DEMI_LONGUEUR_NON_DEPLOYE_AVANT);
		this.depart = new AStarNode(chrono, demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant);
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
		if(!rechercheEnCours)
			throw new NotInitializedPathfindingException("updatePath appelé alors qu'aucune recherche n'est en cours !");

		log.write("Path search begins.", LogCategoryKraken.PF);
		trajetDeSecours = null;
		depart.parent = null;
		depart.cameFromArcDynamique = null;
		depart.g_score = 0;

		Double heuristique = arcmanager.heuristicCostCourbe((depart.robot).getCinematique());

		if(heuristique == null)
		{
			throw new NoPathException("No path found by D* Lite !");
		}

		depart.f_score = heuristique / vitesseMax.getMaxForwardSpeed(0);
		openset.clear();
		openset.add(depart); // Les nœuds à évaluer
		closedset.clear();

		long debutRecherche = System.currentTimeMillis();

		AStarNode current, successeur;
		do
		{
			current = openset.poll();

			/**
			 * needStop ne concerne que la replanif
			 */
			if(replanif && chemin.needStop())
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
				}/*
				if(suppObsFixes)
				{
					CinematiqueObs obsDepart = cinemMemory.getNewNode();
					Cinematique cinemDepart = depart.state.robot.getCinematique();
					obsDepart.updateReel(cinemDepart.getPosition().getX(), cinemDepart.getPosition().getY(), cinemDepart.orientationReelle, cinemDepart.enMarcheAvant, cinemDepart.courbureReelle);
					arcmanager.disableObstaclesFixes(symetrie, obsDepart);
				}*/

				trajetDeSecours = null;
				depart.parent = null;
				depart.cameFromArcDynamique = null;
				depart.g_score = 0;
				heuristique = arcmanager.heuristicCostCourbe((depart.robot).getCinematique());

				if(heuristique == null)
					throw new NoPathException("No path found by the D* Lite");

				depart.f_score = heuristique / vitesseMax.getMaxForwardSpeed(0);

				memorymanager.empty();
				cinemMemory.empty();
				closedset.clear();
				openset.clear();

				current = depart;
			}

			// if(current.getArc() != null)
			// log.debug("Meilleur : "+current.getArc().vitesse);

			// si on a déjà fait ce point ou un point très proche…
			// exception si c'est un point d'arrivée
			if(!closedset.add(current) && !arcmanager.isArrived(current))
			{
//				if(graphicTrajectory || graphicTrajectoryAll)
//					current.setDead();
				if(current != depart)
					destroy(current);
				continue;
			}

			// ce calcul étant un peu lourd, on ne le fait que si le noeud a été
			// choisi, et pas à la sélection des voisins (dans hasNext par
			// exemple)
			if(!arcmanager.isReachable(current))
			{
				if(current != depart)
					destroy(current);
				continue; // collision mécanique attendue. On passe au suivant !
			}

			// affichage
//			if(graphicTrajectory && !graphicTrajectoryAll)
//				buffer.addSupprimable(current);

			// Si current est la trajectoire de secours, ça veut dire que cette
			// trajectoire de secours est la meilleure possible, donc on a fini
			if(current == trajetDeSecours)
			{
				// log.debug("On est arrivé !");
				chemin.setUptodate();
				partialReconstruct(current, chemin);
				memorymanager.empty();
				cinemMemory.empty();
				return;
			}

			long elapsed = System.currentTimeMillis() - debutRecherche;

			if(!rechercheEnCours)
			{
				chemin.setUptodate();
				log.write("The path search has been canceled.", SeverityCategoryKraken.WARNING, LogCategoryKraken.PF);
				return;
			}

			if(!replanif && elapsed > dureeMaxPF) // étant donné
																// qu'il peut
																// continuer
																// jusqu'à
																// l'infini...
			{
				memorymanager.empty();
				cinemMemory.empty();
				if(trajetDeSecours != null) // si on a un trajet de secours, on
											// l'utilise
				{
					log.write("The backup path is used.", LogCategoryKraken.PF);
					chemin.setUptodate();
					partialReconstruct(trajetDeSecours, chemin);
					return;
				}
				throw new TimeoutException("Pathfinding timeout !");
			}

			// On parcourt les voisins de current
			// opensetTmp.clear();
			// closedsetTmp.clear();
			arcmanager.reinitIterator(current);
			while(arcmanager.hasNext())
			{
				successeur = memorymanager.getNewNode();
				successeur.cameFromArcDynamique = null;

				// S'il y a un problème, on passe au suivant (interpolation
				// cubique impossible par exemple)
				if(!arcmanager.next(successeur))
				{
					destroy(successeur);
					continue;
				}
				successeur.parent = current;
				successeur.g_score = current.g_score + arcmanager.distanceTo(successeur, vitesseMax);

				// on a déjà visité un point proche?
				// ceci est vraie seulement si l'heuristique est monotone. C'est
				// normalement le cas.
				if(closedset.contains(successeur))
				{
					destroy(successeur);
					continue;
				}

				// affichage
//				if(graphicTrajectoryAll)
//					buffer.addSupprimable(successeur);

				heuristique = arcmanager.heuristicCostCourbe(successeur.robot.getCinematique());
				if(heuristique == null)
					heuristique = arcmanager.heuristicDirect(successeur.robot.getCinematique());

				successeur.f_score = successeur.g_score + heuristique / vitesseMax.getMaxForwardSpeed(0);

				// noeud d'arrivé
				if(arcmanager.isArrived(successeur) && arcmanager.isReachable(successeur) && (trajetDeSecours == null || trajetDeSecours.f_score > successeur.f_score))
				{
					if(trajetDeSecours != null)
						destroy(trajetDeSecours);
					// log.debug("Arrivée trouvée !");
					trajetDeSecours = successeur;
				}

				openset.add(successeur);
				// opensetTmp.add(successeur);
				// log.debug(successeur.getArc().vitesse+"
				// "+successeur.g_score+"
				// "+(successeur.f_score-successeur.g_score));
			}

			// On ajoute que les meilleurs
			/*
			 * int nbAjoutes = 0;
			 * while(nbAjoutes < tailleFaisceau)
			 * {
			 * AStarCourbeNode n = opensetTmp.poll();
			 * if(n == null) // pas assez de nœuds ? Tant pis
			 * break;
			 * if(closedsetTmp.add(n)) // on vérifie que ce hash n'est pas déjà
			 * utilisé
			 * {
			 * openset.add(n);
			 * nbAjoutes++;
			 * }
			 * }
			 */

		} while(!openset.isEmpty());

		/**
		 * Plus aucun nœud à explorer
		 */
		memorymanager.empty();
		cinemMemory.empty();
		throw new NoPathException("All the space has been searched and no path has been found.");
	}

	private void destroy(AStarNode n)
	{
		if(n.cameFromArcDynamique != null)
			cinemMemory.destroyNode(n.cameFromArcDynamique);
		memorymanager.destroyNode(n);
	}

	private final Cinematique partialReconstruct(AStarNode best, CheminPathfindingInterface chemin)
	{
		return partialReconstruct(best, chemin, 500);
	}

	/**
	 * Reconstruit le chemin. Il peut reconstruire le chemin même si celui-ci
	 * n'est pas fini.
	 * 
	 * @param best
	 * @param last
	 * @throws PathfindingException
	 */
	private final Cinematique partialReconstruct(AStarNode best, CheminPathfindingInterface chemin, int profondeurMax)
	{
		AStarNode noeudParent = best;
		Tentacle arcParent = best.getArc();

		while(noeudParent.parent != null)
		{
			pileTmp.push(arcParent);
			noeudParent = noeudParent.parent;
			arcParent = noeudParent.getArc();
		}
		trajectory.clear();

		Cinematique last = null;
		// chemin.add fait des copies des points
		while(!pileTmp.isEmpty() && profondeurMax > 0)
		{
			Tentacle a = pileTmp.pop();
//			log.write(a.vitesse + " (" + a.getNbPoints() + " pts)", LogCategoryKraken.PF);
			for(int i = 0; i < a.getNbPoints(); i++)
			{
				last = a.getPoint(i);
				trajectory.add(new ItineraryPoint(last));
			}
			assert trajectory.size() < 255 : "Overflow du trajet";
			profondeurMax--;
		}

		pileTmp.clear();
		chemin.addToEnd(trajectory);
		log.write("Research completed.", LogCategoryKraken.PF);

		return last;
	}

	/**
	 * Calcul de chemin classique
	 * 
	 * @param arrivee
	 * @param sens
	 * @param shoot
	 * @throws NoPathException 
	 */
	public void initializeNewSearch(XYO start, XY arrival) throws NoPathException
	{
		vitesseMax = DefaultSpeed.STANDARD;
		depart.init();
		depart.robot.setCinematique(new Cinematique(start));
		arcmanager.configureArcManager(DirectionStrategy.defaultStrategy, arrival);
/*
		if(suppObsFixes)
		{
			CinematiqueObs obsDepart = cinemMemory.getNewNode();
			Cinematique cinemDepart = depart.state.robot.getCinematique();
			obsDepart.updateReel(cinemDepart.getPosition().getX(), cinemDepart.getPosition().getY(), cinemDepart.orientationReelle, cinemDepart.enMarcheAvant, cinemDepart.courbureReelle);
			arcmanager.disableObstaclesFixes(symetrie, obsDepart);
		}*/

		if(!dstarlite.computeNewPath(depart.robot.getCinematique().getPosition(), arrival))
			throw new NoPathException("No path found by D* Lite !");
//		if(graphicDStarLite)
//			dstarlite.itineraireBrut();
		rechercheEnCours = true;
	}

	/**
	 * Calcul de chemin avec arrivée sur un cercle
	 * 
	 * @param state
	 * @param endNode
	 * @param shoot_game_element
	 * @return
	 * @throws PathfindingException
	 * @throws InterruptedException
	 */
/*	public void initializeNewSearchToCircle(RobotState robot) throws PathfindingException, MemoryManagerException
	{
		vitesseMax = DefaultSpeed.STANDARD;
		depart.init();
		robot.copy(depart.robot);
		arcmanager.configureArcManagerWithCircle(DirectionStrategy.defaultStrategy);*/
/*
		if(suppObsFixes)
		{
			CinematiqueObs obsDepart = cinemMemory.getNewNode();
			Cinematique cinemDepart = depart.state.robot.getCinematique();
			obsDepart.updateReel(cinemDepart.getPosition().getX(), cinemDepart.getPosition().getY(), cinemDepart.orientationReelle, cinemDepart.enMarcheAvant, cinemDepart.courbureReelle);
			arcmanager.disableObstaclesFixes(symetrie, obsDepart);
		}*/
/*
		dstarlite.computeNewPath(depart.robot.getCinematique().getPosition(), cercle.arriveeDStarLite);
		if(graphicDStarLite)
			dstarlite.itineraireBrut();
		rechercheEnCours = true;
	}
*/
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
	protected void updatePath(Cinematique lastValid) throws NotInitializedPathfindingException
	{
		// TODO : disabled
		if(!rechercheEnCours)
			throw new NotInitializedPathfindingException("updatePath is called before the initialization !");

		log.write("Replanification lancée", LogCategoryKraken.REPLANIF);

		depart.init();
// TODO		state.copyAStarCourbe(depart.state);

		/*
		 * Forcément, on utilise le vrai chemin ici
		 */

		closedset.clear();
		depart.robot.setCinematique(lastValid);

/*		if(suppObsFixes)
		{
			CinematiqueObs obsDepart = cinemMemory.getNewNode();
			Cinematique cinemDepart = depart.state.robot.getCinematique();
			obsDepart.updateReel(cinemDepart.getPosition().getX(), cinemDepart.getPosition().getY(), cinemDepart.orientationReelle, cinemDepart.enMarcheAvant, cinemDepart.courbureReelle);
			arcmanager.disableObstaclesFixes(symetrie, obsDepart);
		}*/

		// On met à jour le D* Lite
		dstarlite.updateObstacles();

//		process(realChemin, true);
	}
	
/*	public boolean isArrivedAsser()
	{
		return arcmanager.isArrivedAsser(state.robot.getCinematique());
	}*/

}