package planification;

import container.Service;
import permissions.ReadOnly;
import robot.RobotReal;
import strategie.GameState;
import utils.Config;
import utils.Log;

/**
 * Recherche de chemin, avec un algorithme D*
 * @author pf
 * 
 */

public class Pathfinding implements Service {

	protected Log log;
	private MemoryManager memorymanager;
	private GameState<RobotReal,ReadOnly> state;
	
	public Pathfinding(Log log, MemoryManager memorymanager, GameState<RobotReal,ReadOnly> state)
	{
		this.log = log;
		this.memorymanager = memorymanager;
		this.state = state;
	}
	
	/**
	 * Met à jour un chemin
	 * @param depart
	 * @param arrivee
	 * @return
	 */
	public void computePath(Chemin cheminAModifier, GameState<?,ReadOnly> depart, GameState<?,ReadOnly> arrivee, boolean eviteElementsJeu)
	{
		
	}
	
	/**
	 * Calcule un chemin
	 * @param depart
	 * @param arrivee
	 * @return
	 */
	public Chemin computePath(GameState<?,ReadOnly> depart, GameState<?,ReadOnly> arrivee, boolean eviteElementsJeu)
	{
		return null;
	}

	@Override
	public void updateConfig(Config config)
	{
		// TODO
	}
	
}
