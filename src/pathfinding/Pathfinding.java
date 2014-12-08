package pathfinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import container.Service;
import enums.PathfindingNodes;
import exceptions.GridSpaceException;
import exceptions.PathfindingException;
import exceptions.PathfindingRobotInObstacleException;
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
	private static final int COEFF_HEURISTIC = 5;
	
	private Set<PathfindingNodes> openset = new LinkedHashSet<PathfindingNodes>();	 // The set of tentative nodes to be evaluated

	private Log log;
	
	/**
	 * Constructeur du système de recherche de chemin
	 */
	public Pathfinding(Log log, Config config)
	{
		this.log = log;
	}

	/**
	 * Cherche un chemin en ignorant les éléments de jeux. A utiliser si on est coincé.
	 * @param orig
	 * @param indice_point_arrivee
	 * @param gridspace
	 * @return
	 * @throws PathfindingException
	 * @throws PathfindingRobotInObstacleException
	 */
	public ArrayList<PathfindingNodes> computePathForce(Vec2 orig, PathfindingNodes indice_point_arrivee, GridSpace gridspace) throws PathfindingException, PathfindingRobotInObstacleException
	{
		gridspace.setAvoidGameElement(false);
		ArrayList<PathfindingNodes> chemin = computePath(orig, indice_point_arrivee, gridspace);
		gridspace.setAvoidGameElement(true);
		return chemin;
	}
	
	public ArrayList<PathfindingNodes> computePath(Vec2 orig, PathfindingNodes indice_point_arrivee, GridSpace gridspace) throws PathfindingException, PathfindingRobotInObstacleException
	{
		PathfindingNodes indice_point_depart;
		try {
			indice_point_depart = gridspace.nearestReachableNode(orig);
			log.debug("Plus proche de "+orig+": "+indice_point_depart, this);
			ArrayList<PathfindingNodes> chemin = process(indice_point_depart, indice_point_arrivee, gridspace);
			return lissage(orig, chemin, gridspace);
		} catch (GridSpaceException e1) {
			throw new PathfindingRobotInObstacleException();
		}
	}
	
	@Override
	public void updateConfig() {
		// TODO Auto-generated method stub
		
	}
	
	private ArrayList<PathfindingNodes> lissage(Vec2 depart, ArrayList<PathfindingNodes> chemin, GridSpace gridspace)
	{
		// si on peut sauter le premier point, on le fait
		while(chemin.size() >= 2 && gridspace.isTraversable(depart, chemin.get(1).getCoordonnees()))
			chemin.remove(0);
		return chemin;
	}
	
	private ArrayList<PathfindingNodes> process(PathfindingNodes depart, PathfindingNodes arrivee, GridSpace gridspace) throws PathfindingException
	{
		ArrayList<PathfindingNodes> chemin = new ArrayList<PathfindingNodes>();
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

		// TODO: vérifier que c'est bien initialisé à false même sans la boucle
		boolean[] closedset = new boolean[PathfindingNodes.values().length]; // The set of nodes already evaluated.
		for(int i = 0; i < PathfindingNodes.values().length; i++)
			closedset[i] = false;
		
		openset.clear();
		openset.add(depart);	// The set of tentative nodes to be evaluated, initially containing the start node
			
		g_score[depart.ordinal()] = 0;	// Cost from start along best known path.
		// Estimated total cost from start to goal through y.
		f_score[depart.ordinal()] = g_score[depart.ordinal()] + COEFF_HEURISTIC * gridspace.getDistance(depart, arrivee);
		
		PathfindingNodes current, tmp;
		Iterator<PathfindingNodes> nodeIterator = openset.iterator();
		double tentative_g_score = 0;

		while (openset.size() != 0)
		{
			// current is affected by the node in openset having the lowest f_score[] value
			nodeIterator = openset.iterator();
			current = nodeIterator.next();

			while(nodeIterator.hasNext())
			{
				tmp = nodeIterator.next();
				if (f_score[tmp.ordinal()] < f_score[current.ordinal()])
					current  = tmp;
			}

			if(current == arrivee)
			{
				arrivee.incrementUse();
				chemin.add(arrivee);
				tmp = came_from[current.ordinal()];
				while (tmp != depart)
				{
					tmp.incrementUse();
					chemin.add(1, tmp); // insert le point d'avant après l'entrée
			    	tmp = came_from[tmp.ordinal()];
				}
				return chemin;	//  reconstructed path
			}
		    	
			openset.remove(current);
			closedset[current.ordinal()] = true;
			
			gridspace.reinitIterator(current);
		    	
			while(gridspace.hasNext())
			{
				tmp = gridspace.next();

				if(closedset[tmp.ordinal()]) // si closedset contient current
					continue;
				
				tentative_g_score = g_score[current.ordinal()] + gridspace.getDistance(current, tmp);
		    			
				if(!openset.contains(tmp) || tentative_g_score < g_score[tmp.ordinal()])
				{
					came_from[tmp.ordinal()] = current;
					g_score[tmp.ordinal()] = tentative_g_score;
					// TODO: vérifier que 5 est bien le meilleur coefficient
					f_score[tmp.ordinal()] = tentative_g_score + COEFF_HEURISTIC * gridspace.getDistance(tmp, arrivee);
					if(!openset.contains(tmp))
						openset.add(tmp);
				}
			}	
		}
		    		    	
	throw new PathfindingException();
	
	}
	
}