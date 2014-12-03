package pathfinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import container.Service;
import exceptions.GridSpaceException;
import exceptions.PathfindingException;
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
	private GridSpace gridspace;
	private ArrayList<Integer> chemin;	// réceptacle du calcul
		
	private static final int COEFF_HEURISTIC = 5;
	
	private Set<Integer> openset;	 // The set of tentative nodes to be evaluated

	/**
	 * Constructeur du système de recherche de chemin
	 */
	public Pathfinding(Log log, Config config, GridSpace gridspace)
	{
		this.gridspace = gridspace;		
		openset = new LinkedHashSet<Integer>();
		
	}
	
	public ArrayList<Vec2> computePath(Vec2 orig, Vec2 dest) throws PathfindingException
	{
		
		int indice_point_depart, indice_point_arrivee;
		try {
			indice_point_depart = gridspace.nearestReachableNode(orig);
			indice_point_arrivee = gridspace.nearestReachableNode(dest);
			ArrayList<Vec2> chemin = new ArrayList<Vec2>();

			chemin.add(orig);

			ArrayList<Integer> cheminNodes = process(indice_point_depart, indice_point_arrivee);
			for(Integer id: cheminNodes)
				chemin.add(gridspace.getNode(id));
			chemin.add(dest);

			// lissage a besoin de orig
			chemin = lissage(chemin);
			
			chemin.remove(0);
			return chemin;

		} catch (GridSpaceException e) {
			// Aucun point de passage n'est accessible... TODO: trouver une solution
			e.printStackTrace();
			throw new PathfindingException();
		}
	}
	
	private ArrayList<Vec2> lissage(ArrayList<Vec2> chemin)
	{
		int length = chemin.size();
		// Si on a une seule arête, il n'y a pas de lissage possible
		if(length <= 2)
			return chemin;

		// Si on peut sauter le point de passage près de l'arrivée, on le fait
		if(gridspace.isTraversable(chemin.get(length-3), chemin.get(length-1)))
			chemin.remove(length-2);

		return chemin;
	}
	
	@Override
	public void updateConfig() {
		// TODO Auto-generated method stub
		
	}
	
	public ArrayList<Integer> process(int depart, int arrivee) throws PathfindingException
	{
		chemin.clear();
		chemin.add(depart);

		// optimisation si depart == arrivee
		if(depart == arrivee)
			return chemin;
		
		// optimisation si arrivée est directement accessible de départ
		if(gridspace.isTraversable(depart, arrivee))
		{
			chemin.add(arrivee);
			return chemin;
		}

		int[] came_from = new int[GridSpace.NB_NODES]; // The map of navigated nodes.
		double[] g_score = new double[GridSpace.NB_NODES];
		double[] f_score = new double[GridSpace.NB_NODES];

		// TODO: vérifier que c'est bien initialisé à false
		boolean[] closedset = new boolean[GridSpace.NB_NODES]; // The set of nodes already evaluated.		// The set of nodes already evaluated.
		
		openset.clear();
		openset.add(depart);	// The set of tentative nodes to be evaluated, initially containing the start node
			
		g_score[depart] = 0;	// Cost from start along best known path.
		// Estimated total cost from start to goal through y.
		f_score[depart] = g_score[depart] + COEFF_HEURISTIC * gridspace.getDistance(depart, arrivee);
		
		int current, tmp;
		Iterator<Integer> nodeIterator = openset.iterator();
		double tentative_g_score = 0;

		while (openset.size() != 0)
		{
			// current is affected by the node in openset having the lowest f_score[] value
			nodeIterator = openset.iterator();
			current = nodeIterator.next();
			while(nodeIterator.hasNext())
			{
				tmp = nodeIterator.next();
				if (f_score[tmp] < f_score[current])
					current  = tmp;
			}
		    	
			if(current == arrivee)
			{
				chemin.add(arrivee);
				tmp = came_from[current];
				while (tmp != depart)
				{
					chemin.add(0, tmp); // insert le point d'avant au debut du parcours
			    	tmp = came_from[tmp];
				}
				return chemin;	//  reconstructed path
			}
		    	
			openset.remove(current);
			closedset[current] = true;
			
			gridspace.reinitIterator(current);
		    	
			while(gridspace.hasNext())
			{
				tmp = gridspace.next();

				if(closedset[current]) // si closedset contient current
					continue;
				
				tentative_g_score = g_score[current] + gridspace.getDistance(current, tmp);
		    			
				if(!openset.contains(tmp) || tentative_g_score < g_score[tmp])
				{
					came_from[tmp] = current;
					g_score[tmp] = tentative_g_score;
					// TODO: vérifier que 5 est bien le meilleur coefficient
					f_score[tmp] = tentative_g_score + COEFF_HEURISTIC * gridspace.getDistance(tmp, arrivee);
					if(!openset.contains(tmp))
						openset.add(tmp);
		    				
				}
			}	
		}
		    		    	
	throw new PathfindingException();
	
	}
	
}