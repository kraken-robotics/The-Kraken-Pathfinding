package pathfinding.astar;

import pathfinding.GameState;
import permissions.ReadOnly;
import container.Service;
import exceptions.FinMatchException;
import robot.RobotReal;
import utils.Config;
import utils.Log;

/**
 * Classe qui fournit des objets AStarNode à AStar.
 * AStar a besoin de beaucoup de nodes, et l'instanciation d'un objet est long.
 * Du coup on réutilise les mêmes objets sans devoir en créer tout le temps de nouveaux.
 * @author pf
 *
 */

public class AStarMemoryManager implements Service {

	private static final int nb_instances = 500;

	private final AStarNode[] nodes = new AStarNode[nb_instances];
	protected Log log;
	
	// nodes est triés: avant firstAvailable, les gamestate sont indisponibles, après, ils sont disponibles
	private int firstAvailable;
	
	@Override
	public void updateConfig(Config config)
	{
		for(int j = 0; j < nb_instances; j++)
			nodes[j].state.updateConfig(config);
	}

	@Override
	public void useConfig(Config config)
	{}

	public AStarMemoryManager(Log log, GameState<RobotReal,ReadOnly> realstate)
	{	
		this.log = log;

		firstAvailable = 0;
		// on prépare déjà des gamestates
		log.debug("Instanciation de "+nb_instances+" GameState<RobotChrono>");

		for(int i = 1; i < nb_instances; i++)
		{
			nodes[i].state = GameState.cloneGameState(realstate);
		}
		log.debug("Instanciation finie");
	}
	
	/**
	 * Donne un gamestate disponible
	 * @param id_astar
	 * @return
	 * @throws FinMatchException
	 */
	public AStarNode getNewNode() throws FinMatchException
	{
		// lève une exception s'il n'y a plus de place
		AStarNode out;
		out = nodes[firstAvailable];
		firstAvailable++;
		return out;
	}
	
	/**
	 * Signale qu'un gamestate est de nouveau disponible
	 * @param state
	 * @param id_astar
	 * @throws MemoryManagerException
	 */
	public void destroyNode(AStarNode state)
	{
		
		int indice_state = state.getIndiceMemoryManager();
		/**
		 * S'il est déjà détruit, on lève une exception
		 */
//		if(indice_state >= firstAvailable)
//			throw new MemoryManagerException();

		// On inverse dans le Vector les deux gamestates,
		// de manière à avoir toujours un Vector trié.
		firstAvailable--;
		
		AStarNode tmp1 = nodes[indice_state];
		AStarNode tmp2 = nodes[firstAvailable];

		tmp1.setIndiceMemoryManager(firstAvailable);
		tmp2.setIndiceMemoryManager(indice_state);

		nodes[firstAvailable] = tmp1;
		nodes[indice_state] = tmp2;
	}
	
	/**
	 * Signale que tous les gamestates sont disponibles. Très rapide.
	 * @param id_astar
	 */
	public void empty()
	{
		firstAvailable = 0;
	}
	
	/**
	 * Utilisé par les tests
	 */
	public boolean isMemoryManagerEmpty()
	{
		return firstAvailable == 0;
	}
	
}
