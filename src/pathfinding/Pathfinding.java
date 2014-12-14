package pathfinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import container.Service;
import enums.PathfindingNodes;
import exceptions.FinMatchException;
import exceptions.GridSpaceException;
import exceptions.PathfindingException;
import exceptions.PathfindingRobotInObstacleException;
import robot.RobotChrono;
import smartMath.Vec2;
import strategie.GameState;
import utils.Config;
import utils.Log;

/**
 * Classe encapsulant les calculs de pathfinding
 * @author pf, Martial
 *
 */

public class Pathfinding implements Service
{
	/**
	 * Les analogies sont:
	 * un noeud est un GameState<RobotChrono> dans les deux cas
	 * un arc est un script pour l'arbre des possibles,
	 *   un pathfindingnode pour le pathfinding
	 */
	
	private int COEFF_HEURISTIC = 1;
	
	private Set<GameState<RobotChrono>> openset = new LinkedHashSet<GameState<RobotChrono>>();	 // The set of tentative nodes to be evaluated
	private ArrayList<GameState<RobotChrono>> closedset = new ArrayList<GameState<RobotChrono>>();	 // The set of tentative nodes to be evaluated
	private Map<GameState<RobotChrono>, GameState<RobotChrono>>	came_from; // The map of navigated nodes.
	private Map<GameState<RobotChrono>, ArcInterface>	came_from_arc;
	private Map<GameState<RobotChrono>, Double>	g_score, f_score;

//	private Log log;
	
	/**
	 * Constructeur du système de recherche de chemin
	 */
	public Pathfinding(Log log, Config config)
	{
//		this.log = log;
	}
	
	public ArrayList<PathfindingNodes> computePath(GameState<RobotChrono> state, PathfindingNodes indice_point_arrivee, boolean more_points, boolean shoot_game_element) throws PathfindingException, PathfindingRobotInObstacleException, FinMatchException
	{
		try {
			state.gridspace.setAvoidGameElement(!shoot_game_element);
			state.robot.va_au_point_pathfinding(state.gridspace.nearestReachableNode(state.robot.getPosition()), null);
			
			GameState<RobotChrono> arrivee = state.cloneGameState();
			arrivee.robot.setPositionPathfinding(indice_point_arrivee);
			ArrayList<ArcInterface> cheminArc = process(state, arrivee, state.gridspace);
			ArrayList<PathfindingNodes> chemin = new ArrayList<PathfindingNodes>(); 
			for(ArcInterface arc: cheminArc)
				chemin.add((PathfindingNodes)arc);
			return lissage(state.robot.getPosition(), state, chemin);
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
	
	private ArrayList<ArcInterface> process(GameState<RobotChrono> depart, GameState<RobotChrono> arrivee, ArcManagerInterface arcmanager) throws PathfindingException, FinMatchException
	{
		ArrayList<ArcInterface> chemin = new ArrayList<ArcInterface>();

		// optimisation si depart == arrivee
		if(arcmanager.areEquals(depart, arrivee))
			return chemin;

		closedset.clear();
		openset.clear();
		openset.add(depart);	// The set of tentative nodes to be evaluated, initially containing the start node
		came_from_arc.clear();
		came_from.clear();
		g_score.put(depart, 0.);	// Cost from start along best known path.
		// Estimated total cost from start to goal through y.
		f_score.put(depart, g_score.get(depart) + COEFF_HEURISTIC * arcmanager.heuristicCost(depart, arrivee));
		
		GameState<RobotChrono> current;
		Iterator<GameState<RobotChrono>> nodeIterator;
		double tentative_g_score = 0;

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
				if(f_score.get(tmp) < f_score.get(current))
					current = tmp;
			}

			if(arcmanager.areEquals(current, arrivee))
			{
				GameState<RobotChrono> noeud_parent = came_from.get(current);
				ArcInterface arc_parent = came_from_arc.get(current);
				while (noeud_parent != null)
				{
					chemin.add(1, arc_parent); // insert le point d'avant après l'entrée
					arc_parent = came_from_arc.get(noeud_parent);
					noeud_parent = came_from.get(noeud_parent);
				}
				return chemin;	//  reconstructed path
			}

			openset.remove(current);
			closedset.add(current);
			
			arcmanager.reinitIterator(current);
		    	
			while(arcmanager.hasNext())
			{
				ArcInterface voisin = arcmanager.next();
				
				// On construit tmp, qui est un game state, en faisant bouger current.
				// Met automatiquement à jour les obstacles de proximité
				GameState<RobotChrono> successeur = current.cloneGameState();
				
				tentative_g_score = g_score.get(current) + arcmanager.distanceTo(successeur, voisin);
		    			
				if(!openset.contains(successeur) || tentative_g_score < g_score.get(successeur))
				{
					came_from.put(successeur, current);
					came_from_arc.put(successeur, voisin);
					g_score.put(successeur, tentative_g_score);
					f_score.put(successeur, tentative_g_score + COEFF_HEURISTIC * arcmanager.heuristicCost(successeur, arrivee));
					if(!openset.contains(successeur))
						openset.add(successeur);
				}
			}	
		}
		    		    	
	throw new PathfindingException();
	
	}
	
	/**
	 * Recherche de constante
	 */
	public void setHeuristiqueCoeff(int n)
	{
		COEFF_HEURISTIC = n;
	}
		
}