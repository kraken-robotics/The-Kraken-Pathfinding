package planification;

import container.Service;
import permissions.ReadOnly;
import strategie.GameState;

/**
 * Recherche de chemin, avec un algorithme D*
 * @author pf
 * 
 */

public class Pathfinding implements Service {

	public Pathfinding()
	{
	}
	
	/**
	 * Calcule un chemin
	 * @param depart
	 * @param arrivee
	 * @return
	 */
	public void computePath(Chemin cheminAModifier, GameState<?,ReadOnly> depart, GameState<?,ReadOnly> arrivee)
	{
		
	}

	@Override
	public void updateConfig()
	{
		// TODO
	}
	
}
