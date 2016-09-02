package obstacles;

import container.Service;
import obstacles.types.ObstacleRectangular;
import utils.Config;
import utils.Log;

/**
 * Classe qui fournit des objets ObstaclesRectangular
 * Le moteur a besoin de beaucoup de ces obstacles de robot, et l'instanciation d'un objet est long.
 * Du coup on réutilise les mêmes objets sans devoir en créer tout le temps de nouveaux.
 * @author pf
 *
 */

public class ObstaclesRectangularMemory implements Service {

	private static final int nb_instances = 500;

	private final ObstacleRectangular[] nodes = new ObstacleRectangular[nb_instances];
	protected Log log;
	
	private int firstAvailable;
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

	public ObstaclesRectangularMemory(Log log)
	{	
		this.log = log;

		firstAvailable = 0;
		log.debug("Instanciation de "+nb_instances+" ObstaclesRectangular");

		for(int i = 0; i < nb_instances; i++)
			nodes[i] = new ObstacleRectangular();

		log.debug("Instanciation finie");
	}
	
	/**
	 * Donne un gamestate disponible
	 * @param id_astar
	 * @return
	 * @throws FinMatchException
	 */
	public ObstacleRectangular getNewNode()
	{
		// lève une exception s'il n'y a plus de place
		ObstacleRectangular out;
		out = nodes[firstAvailable];
		firstAvailable++;
		return out;
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
	
	/**
	 * Signale qu'un gamestate est de nouveau disponible
	 * @param state
	 * @param id_astar
	 * @throws MemoryManagerException
	 */
	public void destroyNode(ObstacleRectangular state)
	{
		
		int indice_state = state.getIndiceMemoryManager();

		/**
		 * S'il est déjà détruit, on lève une exception
		 */
		if(indice_state >= firstAvailable)
		{
			int z = 0;
			z = 1 / z;
		}

		firstAvailable--;
		
		ObstacleRectangular tmp1 = nodes[indice_state];
		ObstacleRectangular tmp2 = nodes[firstAvailable];

		tmp1.setIndiceMemoryManager(firstAvailable);
		tmp2.setIndiceMemoryManager(indice_state);

		nodes[firstAvailable] = tmp1;
		nodes[indice_state] = tmp2;
	}

	/**
	 * Retourne le nombre d'élément utilisé
	 */
	public int getSize()
	{
		return firstAvailable;
	}
	
}
