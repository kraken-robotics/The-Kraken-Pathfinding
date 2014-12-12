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
	
	private Set<RobotChrono> openset = new LinkedHashSet<RobotChrono>();	 // The set of tentative nodes to be evaluated

	@SuppressWarnings("unused")
	private Log log;
	
	/**
	 * Constructeur du système de recherche de chemin
	 */
	public Pathfinding(Log log, Config config)
	{
		this.log = log;
	}
	
	public ArrayList<PathfindingNodes> computePath(RobotChrono robotchrono, PathfindingNodes indice_point_arrivee, GridSpace gridspace, boolean more_points, boolean shoot_game_element) throws PathfindingException, PathfindingRobotInObstacleException, FinMatchException
	{
		try {
			gridspace.setAvoidGameElement(!shoot_game_element);
			robotchrono.va_au_point_pathfinding(gridspace.nearestReachableNode(robotchrono.getPosition()), null);
			ArrayList<PathfindingNodes> chemin = process(robotchrono, indice_point_arrivee, gridspace, more_points);
			return lissage(robotchrono.getPosition(), robotchrono, chemin, gridspace);
		} catch (GridSpaceException e1) {
			throw new PathfindingRobotInObstacleException();
		}
	}
	
	@Override
	public void updateConfig() {
	}
	
	// Si le point de départ est dans un obstacle fixe, le lissage ne changera rien.
	private ArrayList<PathfindingNodes> lissage(Vec2 depart, RobotChrono robotchrono, ArrayList<PathfindingNodes> chemin, GridSpace gridspace)
	{
		// si on peut sauter le premier point, on le fait
		while(chemin.size() >= 2 && gridspace.isTraversable(depart, chemin.get(1).getCoordonnees()))
		{
			robotchrono.corrige_temps(depart, chemin.get(0).getCoordonnees(), chemin.get(1).getCoordonnees());
			chemin.remove(0);
		}
		return chemin;
	}
	
	private ArrayList<PathfindingNodes> process(RobotChrono robotchrono, PathfindingNodes arrivee, GridSpace gridspace, boolean more_points) throws PathfindingException, FinMatchException
	{
//		compteur = 0;
		ArrayList<PathfindingNodes> chemin = new ArrayList<PathfindingNodes>();
		PathfindingNodes depart = robotchrono.getPositionPathfinding();
		chemin.add(depart);
		depart.incrementUse();

		// optimisation si depart == arrivee
		if(depart == arrivee)
			return chemin;
		
		// optimisation si arrivée est directement accessible de départ
		if(gridspace.isTraversable(depart, arrivee))
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
		openset.add(robotchrono);	// The set of tentative nodes to be evaluated, initially containing the start node
			
		g_score[depart.ordinal()] = 0;	// Cost from start along best known path.
		// Estimated total cost from start to goal through y.
		f_score[depart.ordinal()] = g_score[depart.ordinal()] + COEFF_HEURISTIC * gridspace.getDistance(depart, arrivee);
		
		RobotChrono current, tmp;
		Iterator<RobotChrono> nodeIterator = openset.iterator();
		double tentative_g_score = 0;

		while (openset.size() != 0)
		{
//			compteur++;
			// current is affected by the node in openset having the lowest f_score[] value
			nodeIterator = openset.iterator();
			current = nodeIterator.next();

			while(nodeIterator.hasNext())
			{
				tmp = nodeIterator.next();
				if (f_score[tmp.getPositionPathfinding().ordinal()] < f_score[current.getPositionPathfinding().ordinal()])
					current = tmp;
			}

			if(current.getPositionPathfinding() == arrivee)
			{
				arrivee.incrementUse();
				chemin.add(arrivee);
				PathfindingNodes tmp2 = came_from[current.getPositionPathfinding().ordinal()];
				while (tmp2 != depart)
				{
					tmp2.incrementUse();
					chemin.add(1, tmp2); // insert le point d'avant après l'entrée
			    	tmp2 = came_from[tmp2.ordinal()];
				}
				return chemin;	//  reconstructed path
			}
		    	
			openset.remove(current);
			closedset[current.getPositionPathfinding().ordinal()] = true;
			
			gridspace.reinitIterator(current.getPositionPathfinding(), current.getTempsDepuisDebutMatch());
		    	
			while(gridspace.hasNext(more_points))
			{
				PathfindingNodes voisin = gridspace.next();
				
				if(closedset[voisin.ordinal()]) // si closedset contient current
					continue;
				
				// On construit tmp, qui est un robot chrono, en faisant bouger current.
				tmp = current.cloneIntoRobotChrono();
				tmp.va_au_point_pathfinding(voisin, null);
				
				tentative_g_score = g_score[current.getPositionPathfinding().ordinal()] + gridspace.getDistance(current.getPositionPathfinding(), tmp.getPositionPathfinding());
		    			
				if(!openset.contains(tmp) || tentative_g_score < g_score[tmp.getPositionPathfinding().ordinal()])
				{
					came_from[tmp.getPositionPathfinding().ordinal()] = current.getPositionPathfinding();
					g_score[tmp.getPositionPathfinding().ordinal()] = tentative_g_score;
					f_score[tmp.getPositionPathfinding().ordinal()] = tentative_g_score + COEFF_HEURISTIC * gridspace.getDistance(tmp.getPositionPathfinding(), arrivee);
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