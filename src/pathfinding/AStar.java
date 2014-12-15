package pathfinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import container.Service;
import enums.PathfindingNodes;
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
	private ArrayList<Double> closedset = new ArrayList<Double>();	 // The set of tentative nodes to be evaluated
	private Map<Double, Double>	came_from = new HashMap<Double, Double>(); // The map of navigated nodes.
	private Map<Double, Arc>	came_from_arc = new HashMap<Double, Arc>();
	private Map<Double, Double>	g_score = new HashMap<Double, Double>(), 
				f_score = new HashMap<Double, Double>();

//	private Log log;
	private PathfindingArcManager pfarcmanager;
	private StrategyArcManager stratarcmanager;
	
	/**
	 * Constructeur du système de recherche de chemin
	 */
	public AStar(Log log, Config config, PathfindingArcManager pfarcmanager, StrategyArcManager stratarcmanager)
	{
//		this.log = log;
		this.pfarcmanager = pfarcmanager;
		this.stratarcmanager = stratarcmanager;
	}
	
	public ArrayList<Decision> computeStrategy(GameState<RobotChrono> state) throws FinMatchException, PathfindingException
	{
		// TODO
		GameState<RobotChrono> arrivee = state.cloneGameState();
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
			state.robot.va_au_point_pathfinding(pointDepart, null);
			
			GameState<RobotChrono> arrivee = state.cloneGameState();
			arrivee.robot.setPositionPathfinding(indice_point_arrivee);
			ArrayList<Arc> cheminArc = process(state, arrivee, pfarcmanager);
			ArrayList<PathfindingNodes> chemin = new ArrayList<PathfindingNodes>(); 
			chemin.add(pointDepart);
			for(Arc arc: cheminArc)
				chemin.add((PathfindingNodes)arc);
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
			state.robot.corrige_temps(depart, chemin.get(0).getCoordonnees(), chemin.get(1).getCoordonnees());
			chemin.remove(0);
		}
		return chemin;
	}
	
	private ArrayList<Arc> process(GameState<RobotChrono> depart, GameState<RobotChrono> arrivee, ArcManager arcmanager) throws PathfindingException, FinMatchException
	{
		ArrayList<Arc> chemin = new ArrayList<Arc>();

		// optimisation si depart == arrivee
		if(arcmanager.getHash(depart) == arcmanager.getHash(arrivee))
			return chemin;

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

		// TODO: modifier le state à renvoyer

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
			{
				// On renvoie le robot final, celui qui a parcouru toutes les épreuves, current.
				current.copy(depart);
				Double noeud_parent = came_from.get(arcmanager.getHash(current));
				Arc arc_parent = came_from_arc.get(arcmanager.getHash(current));
				while (noeud_parent != null)
				{
					chemin.add(0, arc_parent);
					arc_parent = came_from_arc.get(noeud_parent);
					noeud_parent = came_from.get(noeud_parent);
				}
				return chemin;	//  reconstructed path
			}

			openset.remove(current);
			closedset.add(arcmanager.getHash(current));
			
			arcmanager.reinitIterator(current);
		    	
			while(arcmanager.hasNext(current))
			{
				Arc voisin = arcmanager.next();
				
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
		    		    	
	throw new PathfindingException();
	
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