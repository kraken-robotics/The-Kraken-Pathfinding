package pathfinding.thetastar;

import java.util.BitSet;
import java.util.PriorityQueue;

import pathfinding.GameState;
import pathfinding.dstarlite.DStarLite;
import pathfinding.dstarlite.GridSpace;
import permissions.ReadOnly;
import permissions.ReadWrite;
import container.Service;
import exceptions.PathfindingException;
import robot.DirectionStrategy;
import robot.RobotChrono;
import robot.RobotReal;
import tests.graphicLib.Fenetre;
import utils.Config;
import utils.Log;

/**
 * Theta*, qui lisse le résultat du D* Lite et fournit une trajectoire courbe
 * @author pf
 *
 */

public class ThetaStar implements Service
{
	private PriorityQueue<ThetaStarNode> openset = new PriorityQueue<ThetaStarNode>(GridSpace.NB_POINTS, new ThetaStarNodeComparator());	 // The set of tentative nodes to be evaluated
	private BitSet closedset = new BitSet(GridSpace.NB_POINTS);
	
	private ThetaStarNode[] memory = new ThetaStarNode[GridSpace.NB_POINTS];
	private DirectionStrategy directionstrategyactuelle;
	
	protected Log log;
	private DStarLite dstarlite;
	private ThetaStarArcManager arcmanager;
	private GameState<RobotReal,ReadOnly> state;
	private CheminPathfinding cheminContainer;
	private GameState<RobotChrono,ReadWrite> stateSuccesseur;
	private long nbPF = 0;
	private Fenetre fenetre;
	private int hashArrivee;

	/**
	 * Constructeur du ThetaStar
	 */
	public ThetaStar(Log log, DStarLite dstarlite, ThetaStarArcManager arcmanager, GameState<RobotReal,ReadOnly> state, CheminPathfinding chemin)
	{
		this.log = log;
		this.dstarlite = dstarlite;
		this.arcmanager = arcmanager;
		this.state = state;
		this.cheminContainer = chemin;

		stateSuccesseur = GameState.cloneGameState(state);

		for(int i = 0; i < GridSpace.NB_POINTS; i++)
			memory[i] = new ThetaStarNode(GameState.cloneGameState(state), i);

		if(Config.graphicThetaStar)
			fenetre = Fenetre.getInstance();
	}

	/**
	 * Calcul d'un chemin à partir d'un certain état (state) et d'un point d'arrivée (endNode).
	 * Le boolean permet de signaler au pathfinding si on autorise ou non le shootage d'élément de jeu pas déjà pris.
	 * @param state
	 * @param endNode
	 * @param shoot_game_element
	 * @return
	 * @throws PathfindingException 
	 */
	public synchronized void computeNewPath(int arrivee, boolean ejecteGameElement, DirectionStrategy directionstrategy) throws PathfindingException
	{
		if(Config.graphicThetaStar)
			fenetre.setColor(arrivee, Fenetre.Couleur.VIOLET);

		this.directionstrategyactuelle = directionstrategy;
		arcmanager.setEjecteGameElement(ejecteGameElement);
		
		ThetaStarNode depart;
		synchronized(state)
		{
			depart = memory[state.robot.getPositionGridSpace()];
			GameState.copyThetaStar(state, depart.state);
		}
		
		dstarlite.computeNewPath(depart.gridpoint, arrivee);
		process(depart);
		
		if(Config.graphicThetaStar)
			printChemin();
	}
	
	private void printChemin()
	{
//		LocomotionArc[] cheminAff = cheminContainer.get();	
//		for(LocomotionArc arc : cheminAff)
//			fenetre.setColor(arc.getGridpointArrivee(), Fenetre.Couleur.VIOLET);
	}
	
	public synchronized void updatePath() throws PathfindingException
	{

		ThetaStarNode depart;
		synchronized(state)
		{
			depart = memory[state.robot.getPositionGridSpace()];
			GameState.copyThetaStar(state, depart.state);
		}
		
		dstarlite.updatePath(depart.gridpoint);
		process(depart);
		
		if(Config.graphicThetaStar)
			printChemin();
	}
		
	/**
	 * Le calcul du ThetaStar
	 * @param depart
	 * @return
	 */
	private void process(ThetaStarNode depart)
	{
		nbPF++;
		closedset.clear();
		depart.came_from = null;
		depart.came_from_arc = null;
		depart.nbPF = nbPF;
		depart.g_score = 0;
		depart.f_score = arcmanager.heuristicCostThetaStar(depart.state.getReadOnly());
		hashArrivee = dstarlite.getHashArrivee();

		cheminContainer.notUptodate();
		arcmanager.emptyMemoryManager();

		openset.clear();
		openset.add(depart);	// The set of tentative nodes to be evaluated, initially containing the start node

		ThetaStarNode current, best;

		do
		{
			current = openset.poll();
			best = current;
			
			if(closedset.get(current.gridpoint))
			{
//				arcmanager.destroyArc(current.came_from_arc);
				continue;
			}

			if(Config.graphicThetaStar)
				fenetre.setColor(current.gridpoint, Fenetre.Couleur.JAUNE);
			
			closedset.set(current.gridpoint);

//			log.debug(current);
			
			// Si on est arrivé, on reconstruit le chemin
			if(current.gridpoint == hashArrivee)
			{
//				log.debug("On est arrivé !");
				partialReconstruct(best);
			}
			
			// On parcourt les voisins de current et de son prédecesseur.

			arcmanager.reinitIterator(current.came_from, current, directionstrategyactuelle);
			
			while(arcmanager.hasNext())
			{
				if(cheminContainer.isNeededToStartAgain())
				{
					partialReconstruct(best);
					openset.clear();
					openset.add(best);
					break;
				}

				LocomotionArc voisin = arcmanager.nextProposition();
//				log.debug("Voisin : "+voisin);
				if(closedset.get(voisin.getGridpointArrivee()) || !arcmanager.nextAccepted())
				{
//					arcmanager.destroyArc(voisin);
					continue;
				}
				
				current = arcmanager.noeudPere();

				GameState.copyThetaStar(current.state.getReadOnly(), stateSuccesseur);

				// stateSuccesseur est modifié lors du "distanceTo"
				int tentative_g_score = current.g_score + arcmanager.distanceTo(stateSuccesseur, voisin);
				int heuristique = arcmanager.heuristicCostThetaStar(stateSuccesseur.getReadOnly());
				int tentative_f_score = tentative_g_score + heuristique;
				
				ThetaStarNode successeur = getFromMemory(stateSuccesseur.robot.getPositionGridSpace());
				
				/**
				 * Normalement, il suffit de comparer les g_scores.
				 * En effet, l'heuristique d'un point est censé être unique, et alors comparer f1 à
				 * f2 revient à comparer f1 - h = g1 à f2 - h = g2.
				 * Or, c'est ici l'heuristique dépend de l'orientation du robot, et donc n'est pas unique pour un gridpoint.
				 * Il est donc nécessaire de comparer les deux f_scores
				 */
				if(tentative_f_score < successeur.f_score)
				{
					if(Config.graphicThetaStar)
						fenetre.setColor(successeur.gridpoint, Fenetre.Couleur.BLEU);

					GameState.copyThetaStar(stateSuccesseur.getReadOnly(), successeur.state);
					successeur.came_from = current;
//					if(successeur.came_from_arc != null)
//						arcmanager.destroyArc(successeur.came_from_arc);
					successeur.came_from_arc = voisin;
					successeur.g_score = tentative_g_score;
					successeur.f_score = tentative_f_score;
//					log.debug(tentative_f_score+" "+tentative_g_score);
//					if(openset.contains(successeur))
//						openset.remove(successeur);
					openset.add(successeur);
				}
//				else
//					arcmanager.destroyArc(voisin);
				
				if(Config.graphicThetaStar)
					fenetre.setColor(current.gridpoint, Fenetre.Couleur.ROUGE);

			}
			
		} while(!openset.isEmpty());
		
		/**
		 * Impossible car DStarLite a déjà trouvé un chemin.
		 */
		log.critical("ThetaStar n'a pas trouvé de chemin !");
		return;
	}
	
	private void partialReconstruct(ThetaStarNode best)
	{
		synchronized(cheminContainer)
		{
			int k = 0;
//			LocomotionArc[] chemin = cheminContainer.get();
			ThetaStarNode noeud_parent = best;
			LocomotionArc arc_parent = best.came_from_arc;
			while(arc_parent != null)
			{
	//			log.debug(arc_parent);
//				arc_parent.copy(chemin[k]);
				k++;
				noeud_parent = noeud_parent.came_from;
				arc_parent = noeud_parent.came_from_arc;
			}
			cheminContainer.setDernierIndiceChemin(k-1);
		}
		openset.clear();
		openset.add(best);
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
	private ThetaStarNode getFromMemory(int hash)
	{
		ThetaStarNode out = memory[hash];
		
		/**
		 * Si ce point n'a pas encore été utilisé pour ce pathfinding, on l'initialise
		 */
		if(out.nbPF != nbPF)
		{
			out.init();
			out.nbPF = nbPF;
		}
		return out;
	}
		
}