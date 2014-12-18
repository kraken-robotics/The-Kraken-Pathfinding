package pathfinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import container.Service;
import enums.GameElementNames;
import enums.PathfindingNodes;
import enums.Tribool;
import exceptions.FinMatchException;
import exceptions.GridSpaceException;
import exceptions.PathfindingException;
import exceptions.PathfindingRobotInObstacleException;
import robot.RobotChrono;
import scripts.Decision;
import smartMath.Vec2;
import strategie.GameState;
import utils.Config;
import utils.Log;

/**
 * Classe encapsulant les calculs de pathfinding
 * @author pf, Martial
 *
 */

public class AStar implements Service
{
	/**
	 * Les analogies sont:
	 * un noeud est un GameState<RobotChrono> dans les deux cas
	 * un arc est un script pour l'arbre des possibles,
	 *   un pathfindingnode pour le pathfinding
	 */
	
	private int COEFF_HEURISTIC = 1;
	
	private ArrayList<GameState<RobotChrono>> openset = new ArrayList<GameState<RobotChrono>>();	 // The set of tentative nodes to be evaluated
	private ArrayList<Integer> closedset = new ArrayList<Integer>();	 // The set of tentative nodes to be evaluated
	private Map<Integer, Integer>	came_from = new HashMap<Integer, Integer>(); // The map of navigated nodes.
	private Map<Integer, Arc>	came_from_arc = new HashMap<Integer, Arc>();
	private Map<Integer, Double>	g_score = new HashMap<Integer, Double>(), 
				f_score = new HashMap<Integer, Double>();
	
	private Log log;
	private PathfindingArcManager pfarcmanager;
	private StrategyArcManager stratarcmanager;
	
	/**
	 * Constructeur du système de recherche de chemin
	 */
	public AStar(Log log, Config config, PathfindingArcManager pfarcmanager, StrategyArcManager stratarcmanager)
	{
		this.log = log;
		this.pfarcmanager = pfarcmanager;
		this.stratarcmanager = stratarcmanager;
		// Afin d'outrepasser la dépendance circulaire qui provient de la double
		// utilisation de l'AStar (stratégie et pathfinding)
		stratarcmanager.setAStar(this);
	}
	
	public ArrayList<Decision> computeStrategy(GameState<RobotChrono> state) throws FinMatchException, PathfindingException
	{
		// TODO
		GameState<RobotChrono> arrivee = state.cloneGameState();
		arrivee.robot.setFinalState();
		ArrayList<Arc> cheminArc = process(state, arrivee, stratarcmanager);
		ArrayList<Decision> decisions = new ArrayList<Decision>();
		for(Arc arc: cheminArc)
			decisions.add((Decision)arc);
		return decisions;
	}
	
	public ArrayList<PathfindingNodes> computePath(GameState<RobotChrono> state, PathfindingNodes indice_point_arrivee, boolean shoot_game_element) throws PathfindingException, PathfindingRobotInObstacleException, FinMatchException
	{
		try {
			Vec2 positionInitiale = state.robot.getPosition();
			state.gridspace.setAvoidGameElement(!shoot_game_element);
			PathfindingNodes pointDepart = state.gridspace.nearestReachableNode(state.robot.getPosition());

			GameState<RobotChrono> depart = state.cloneGameState();
			depart.robot.setPositionPathfinding(pointDepart);
			
			// On pourrait alléger cela... en ayant un gamestate complètement vide, sauf la position
			GameState<RobotChrono> arrivee = state.cloneGameState();
			arrivee.robot.setPositionPathfinding(indice_point_arrivee);
			ArrayList<Arc> cheminArc = process(depart, arrivee, pfarcmanager);
			ArrayList<PathfindingNodes> chemin = new ArrayList<PathfindingNodes>(); 
			chemin.add(pointDepart);
			for(Arc arc: cheminArc)
				chemin.add((PathfindingNodes)arc);

			// si le chemin renvoyé est incomplet, on annule tout
			if(chemin.get(chemin.size()-1) != indice_point_arrivee)
				throw new PathfindingException();

			return lissage(positionInitiale, state, chemin);
		} catch (GridSpaceException e1) {
			throw new PathfindingRobotInObstacleException();
		}
	}
	
	@Override
	public void updateConfig() {
	}
	
	// Si le point de départ est dans un obstacle fixe, le lissage ne changera rien.
	private ArrayList<PathfindingNodes> lissage(Vec2 depart, GameState<RobotChrono> state, ArrayList<PathfindingNodes> chemin)
	{
		// si on peut sauter le premier point, on le fait
		while(chemin.size() >= 2 && state.gridspace.isTraversable(depart, chemin.get(1).getCoordonnees()))
		{
			chemin.remove(0);
		}
		return chemin;
	}
	
	private ArrayList<Arc> process(GameState<RobotChrono> depart, GameState<RobotChrono> arrivee, ArcManager arcmanager) throws PathfindingException, FinMatchException
	{
		// optimisation si depart == arrivee
		if(arcmanager.getHash(depart) == arcmanager.getHash(arrivee))
			return new ArrayList<Arc>();

		closedset.clear();
		openset.clear();
		openset.add(depart);	// The set of tentative nodes to be evaluated, initially containing the start node
		came_from_arc.clear();
		came_from.clear();
		g_score.put(arcmanager.getHash(depart), 0.);	// Cost from start along best known path.
		// Estimated total cost from start to goal through y.
		f_score.put(arcmanager.getHash(depart), g_score.get(arcmanager.getHash(depart)) + COEFF_HEURISTIC * arcmanager.heuristicCost(depart, arrivee));
		
		GameState<RobotChrono> current;
		Iterator<GameState<RobotChrono>> nodeIterator;

		while (openset.size() != 0)
		{
			// TODO: openset trié automatiquement à l'insertion
			// current is affected by the node in openset having the lowest f_score[] value
			nodeIterator = openset.iterator();
			current = nodeIterator.next();

			while(nodeIterator.hasNext())
			{
				GameState<RobotChrono> tmp = nodeIterator.next();
				if(f_score.get(arcmanager.getHash(tmp)) < f_score.get(arcmanager.getHash((current))))
					current = tmp;
			}

			if(arcmanager.getHash(current) == arcmanager.getHash(arrivee))
				return reconstruct(arcmanager.getHash(current));

			openset.remove(current);
			closedset.add(arcmanager.getHash(current));
			
			arcmanager.reinitIterator(current);
		    	
			while(arcmanager.hasNext(current))
			{
				Arc voisin = arcmanager.next();
//				log.debug("Voisin de "+current.robot.getPositionPathfinding()+": "+voisin, this);
				GameState<RobotChrono> successeur = current.cloneGameState();
				
				// successeur est modifié lors du "distanceTo"
				double tentative_g_score = g_score.get(arcmanager.getHash(current)) + arcmanager.distanceTo(successeur, voisin);
				
				if(closedset.contains(arcmanager.getHash(successeur)))
					continue;

				if(!contains(openset, successeur, arcmanager) || tentative_g_score < g_score.get(arcmanager.getHash(successeur)))
				{
					came_from.put(arcmanager.getHash(successeur), arcmanager.getHash(current));
					came_from_arc.put(arcmanager.getHash(successeur), voisin);
					g_score.put(arcmanager.getHash(successeur), tentative_g_score);
					f_score.put(arcmanager.getHash(successeur), tentative_g_score + COEFF_HEURISTIC * arcmanager.heuristicCost(successeur, arrivee));
					if(!contains(openset, successeur, arcmanager))
						openset.add(successeur);
				}
			}	
		}

		/**
		 * Même si on n'a pas atteint l'objectif, on reconstruit un chemin partiel
		 */
		Integer best = null;
		for(Integer h: f_score.keySet())
		{
			if(best == null || f_score.get(h) < f_score.get(best))
				best = h;
		}
		if(best != null)
		{
			log.warning("Reconstruction partielle", this);
			return reconstruct(best);
		}
		
	throw new PathfindingException();
	
	}
	
	
	private ArrayList<Arc> reconstruct(int hash) {
		ArrayList<Arc> chemin = new ArrayList<Arc>();
		Integer noeud_parent = came_from.get(hash);
		Arc arc_parent = came_from_arc.get(hash);
		while (noeud_parent != null)
		{
			chemin.add(0, arc_parent);
			arc_parent = came_from_arc.get(noeud_parent);
			noeud_parent = came_from.get(noeud_parent);
		}
		return chemin;	//  reconstructed path
	}

	/**
	 * Return true si openset contient successeur, compte tenu de l'égalité de
	 * gamestate fournie par l'arcmanager.
	 * @param openset
	 * @param successeur
	 * @param arcmanager
	 * @return
	 */
	private boolean contains(ArrayList<GameState<RobotChrono>> openset,
			GameState<RobotChrono> successeur, ArcManager arcmanager) {
		for(GameState<RobotChrono> state: openset)
			if(arcmanager.getHash(successeur) == arcmanager.getHash(state))
				return true;
		return false;
	}

	/**
	 * Recherche de constante
	 */
	public void setHeuristiqueCoeff(int n)
	{
		COEFF_HEURISTIC = n;
	}
		
}