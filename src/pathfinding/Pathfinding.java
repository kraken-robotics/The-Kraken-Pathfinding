package pathfinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
	private int COEFF_HEURISTIC = 1;
	private int compteur;
	
	private Set<GameState<RobotChrono>> openset = new LinkedHashSet<GameState<RobotChrono>>();	 // The set of tentative nodes to be evaluated

	@SuppressWarnings("unused")
	private Log log;
	
	/**
	 * Constructeur du système de recherche de chemin
	 */
	public Pathfinding(Log log, Config config)
	{
		this.log = log;
	}
	
	public ArrayList<PathfindingNodes> computePath(GameState<RobotChrono> state, PathfindingNodes indice_point_arrivee, boolean more_points, boolean shoot_game_element) throws PathfindingException, PathfindingRobotInObstacleException, FinMatchException
	{
		try {
			state.gridspace.setAvoidGameElement(!shoot_game_element);
			state.robot.va_au_point_pathfinding(state.gridspace.nearestReachableNode(state.robot.getPosition()), null);
			ArrayList<PathfindingNodes> chemin = process(state, indice_point_arrivee, more_points);
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
	
	private ArrayList<PathfindingNodes> process(GameState<RobotChrono> state, PathfindingNodes arrivee, boolean more_points) throws PathfindingException, FinMatchException
	{
//		compteur = 0;
		ArrayList<PathfindingNodes> chemin = new ArrayList<PathfindingNodes>();
		PathfindingNodes depart = state.robot.getPositionPathfinding();
		chemin.add(depart);
		depart.incrementUse();

		// optimisation si depart == arrivee
		if(depart == arrivee)
			return chemin;
		
		// optimisation si arrivée est directement accessible de départ
		if(state.gridspace.isTraversable(depart, arrivee))
		{
			chemin.add(arrivee);
			return chemin;
		}

		PathfindingNodes[] came_from = new PathfindingNodes[PathfindingNodes.values().length]; // The map of navigated nodes.
		double[] g_score = new double[PathfindingNodes.values().length];
		double[] f_score = new double[PathfindingNodes.values().length];

		boolean[] closedset = new boolean[PathfindingNodes.values().length]; // The set of nodes already evaluated.
		for(int i = 0; i < PathfindingNodes.values().length; i++)
			closedset[i] = false;
		
		openset.clear();
		openset.add(state);	// The set of tentative nodes to be evaluated, initially containing the start node
			
		g_score[depart.ordinal()] = 0;	// Cost from start along best known path.
		// Estimated total cost from start to goal through y.
		f_score[depart.ordinal()] = g_score[depart.ordinal()] + COEFF_HEURISTIC * depart.heuristicCost(arrivee);
		
		GameState<RobotChrono> current, tmp;
		Iterator<GameState<RobotChrono>> nodeIterator = openset.iterator();
		double tentative_g_score = 0;

		// TODO: modifier le state à renvoyer
		
		while (openset.size() != 0)
		{
//			compteur++;
			// current is affected by the node in openset having the lowest f_score[] value
			nodeIterator = openset.iterator();
			current = nodeIterator.next();

			while(nodeIterator.hasNext())
			{
				tmp = nodeIterator.next();
				if (f_score[tmp.robot.getPositionPathfinding().ordinal()] < f_score[current.robot.getPositionPathfinding().ordinal()])
					current = tmp;
			}

			if(current.robot.getPositionPathfinding() == arrivee)
			{
				arrivee.incrementUse();
				chemin.add(arrivee);
				PathfindingNodes tmp2 = came_from[current.robot.getPositionPathfinding().ordinal()];
				while (tmp2 != depart)
				{
					tmp2.incrementUse();
					chemin.add(1, tmp2); // insert le point d'avant après l'entrée
			    	tmp2 = came_from[tmp2.ordinal()];
				}
				return chemin;	//  reconstructed path
			}

			openset.remove(current);
			closedset[current.robot.getPositionPathfinding().ordinal()] = true;
			
			state.gridspace.reinitIterator(current.robot.getPositionPathfinding());
		    	
			while(state.gridspace.hasNext())
			{
				PathfindingNodes voisin = state.gridspace.next();
				
				if(closedset[voisin.ordinal()]) // si closedset contient current
					continue;
				
				// On construit tmp, qui est un game state, en faisant bouger current.
				// Met automatiquement à jour les obstacles de proximité
				tmp = current.cloneGameState();
				tmp.robot.va_au_point_pathfinding(voisin, null);
				
				tentative_g_score = g_score[current.robot.getPositionPathfinding().ordinal()] + current.robot.getPositionPathfinding().distanceTo(tmp.robot.getPositionPathfinding());
		    			
				if(!openset.contains(tmp) || tentative_g_score < g_score[tmp.robot.getPositionPathfinding().ordinal()])
				{
					came_from[tmp.robot.getPositionPathfinding().ordinal()] = current.robot.getPositionPathfinding();
					g_score[tmp.robot.getPositionPathfinding().ordinal()] = tentative_g_score;
					f_score[tmp.robot.getPositionPathfinding().ordinal()] = tentative_g_score + COEFF_HEURISTIC * tmp.robot.getPositionPathfinding().heuristicCost(arrivee);
					if(!openset.contains(tmp))
						openset.add(tmp);
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
	
	/**
	 * Recherche de constante
	 * @return
	 */
	public int getCompteur()
	{
		return compteur;
	}
	
}