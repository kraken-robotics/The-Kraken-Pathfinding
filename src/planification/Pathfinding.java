package planification;

import container.Service;
import permissions.ReadOnly;
import planification.astar.arc.Arc;
import strategie.GameState;

public class Pathfinding<A extends Arc> implements Service {

	public Pathfinding()
	{
	}
	
	/**
	 * Calcule un chemin
	 * @param depart
	 * @param arrivee
	 * @return
	 */
	public void computePath(Path<A> cheminAModifier, GameState<?,ReadOnly> depart, GameState<?,ReadOnly> arrivee)
	{
		
	}

	@Override
	public void updateConfig()
	{
		// TODO
	}
	
}
