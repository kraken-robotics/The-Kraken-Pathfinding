package strategie;

import permissions.ReadOnly;
import permissions.ReadWrite;
import container.Service;
import exceptions.FinMatchException;
import robot.RobotChrono;
import robot.RobotReal;
import utils.Config;
import utils.Log;

/**
 * Classe qui fournit des objets GameState à AStar.
 * AStar a besoin de beaucoup de gamestate, et l'instanciation d'un objet est long.
 * Du coup on réutilise les mêmes objets sans devoir en créer tout le temps de nouveaux.
 * @author pf
 *
 */

public class MemoryManager implements Service {

	private static final int nb_instances = 500;

	@SuppressWarnings("unchecked")
	private final GameState<RobotChrono,ReadWrite>[] gamestates_list = (GameState<RobotChrono,ReadWrite>[]) new GameState[nb_instances];
	protected Log log;
	
	// gamestates_list est triés: avant firstAvailable, les gamestate sont indisponibles, après, ils sont disponibles
	private int firstAvailable;
	
	@Override
	public void updateConfig(Config config)
	{
		for(int j = 0; j < nb_instances; j++)
			gamestates_list[j].updateConfig(config);
	}

	@Override
	public void useConfig(Config config)
	{}

	public MemoryManager(Log log, GameState<RobotReal,ReadOnly> realstate)
	{	
		this.log = log;

		firstAvailable = 0;
		// on prépare déjà des gamestates
		log.debug("Instanciation de "+nb_instances+" GameState<RobotChrono>");

		for(int i = 0; i < nb_instances; i++)
		{
			gamestates_list[i] = GameState.cloneGameState(realstate, i);
		}
		log.debug("Instanciation finie");
	}
	
	/**
	 * Donne un gamestate disponible
	 * @param id_astar
	 * @return
	 * @throws FinMatchException
	 */
	public GameState<RobotChrono,ReadWrite> getNewGameState()
	{
		// lève une exception s'il n'y a plus de place
		GameState<RobotChrono,ReadWrite> out;
		out = gamestates_list[firstAvailable];
		firstAvailable++;
		return out;
	}
	
	/**
	 * Signale qu'un gamestate est de nouveau disponible
	 * @param state
	 * @param id_astar
	 */
	public void destroyGameState(GameState<RobotChrono,ReadWrite> state)
	{
		int indice_state = GameState.getIndiceMemoryManager(state.getReadOnly());
		/**
		 * S'il est déjà détruit, on lève une exception
		 */
		if(indice_state >= firstAvailable)
		{
			log.critical("MemoryManager veut détruire un objet déjà détruit !");
		}

		// On inverse dans le Vector les deux gamestates,
		// de manière à avoir toujours un Vector trié.
		firstAvailable--;
		
		GameState<RobotChrono,ReadWrite> tmp1 = gamestates_list[indice_state];
		GameState<RobotChrono,ReadWrite> tmp2 = gamestates_list[firstAvailable];

		GameState.setIndiceMemoryManager(tmp1, firstAvailable);
		GameState.setIndiceMemoryManager(tmp2, indice_state);

		gamestates_list[firstAvailable] = tmp1;
		gamestates_list[indice_state] = tmp2;
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
