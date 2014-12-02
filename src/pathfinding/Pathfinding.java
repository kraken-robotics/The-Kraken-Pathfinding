package pathfinding;

import java.util.ArrayList;

import container.Service;
import exceptions.PathfindingException;
import smartMath.Vec2;
import utils.Config;
import utils.Log;

/**
 * Classe encapsulant les calculs de pathfinding
 * @author pf
 *
 */

public class Pathfinding implements Service
{
	GridSpace gridspace;
	
	/**
	 * Constructeur du système de recherche de chemin
	 */
	public Pathfinding(Log log, Config config, GridSpace gridspace)
	{
		this.gridspace = gridspace;
	}
	
	public ArrayList<Vec2> computePath(Vec2 orig, Vec2 dest) throws PathfindingException
	{
		Vec2 point_depart = gridspace.nearestReachableNode(orig);
		
		// Aucun point de passage n'est accessible... TODO: trouver une solution
		if(point_depart == null)
			throw new PathfindingException();
		
	}
	
	@Override
	public void updateConfig() {
		// TODO Auto-generated method stub
		
	}
}
