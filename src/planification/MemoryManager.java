package planification;

import permissions.ReadOnly;
import permissions.ReadWrite;
import container.Service;
import exceptions.FinMatchException;
import exceptions.MemoryManagerException;
import robot.RobotChrono;
import robot.RobotReal;
import strategie.GameState;
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

	int nb_planif = PlanificateurId.values().length;
	@SuppressWarnings("unchecked")
	private final GameState<RobotChrono,ReadWrite>[][] gamestates_list = (GameState<RobotChrono,ReadWrite>[][]) new GameState[nb_planif][nb_instances];
	protected Log log;
	
	// gamestates_list est triés: avant firstAvailable, les gamestate sont indisponibles, après, ils sont disponibles
	private int firstAvailable[] = new int[nb_planif];
	
	@Override
	public void updateConfig()
	{}

	public MemoryManager(Log log, Config config, GameState<RobotReal,ReadOnly> realstate)
	{	
		this.log = log;

		firstAvailable[0] = 0;
		firstAvailable[1] = 0;
		// on prépare déjà des gamestates
		log.debug("Instanciation de "+nb_planif*nb_instances+" GameState<RobotChrono>");

		for(int j = 0; j < nb_planif; j++)
			for(int i = 0; i < nb_instances; i++)
			{
				gamestates_list[j][i] = GameState.cloneGameState(realstate, i);
			}
		log.debug("Instanciation finie");
		updateConfig();
	}
	
	/**
	 * Donne un gamestate disponible
	 * @param id_astar
	 * @return
	 * @throws FinMatchException
	 */
	public GameState<RobotChrono,ReadWrite> getNewGameState(int id_astar) throws FinMatchException
	{
		// lève une exception s'il n'y a plus de place
		GameState<RobotChrono,ReadWrite> out;
		out = gamestates_list[id_astar][firstAvailable[id_astar]];
		firstAvailable[id_astar]++;
		return out;
	}
	
	/**
	 * Signale qu'un gamestate est de nouveau disponible
	 * @param state
	 * @param id_astar
	 * @throws MemoryManagerException
	 */
	public void destroyGameState(GameState<RobotChrono,ReadWrite> state, int id_astar) throws MemoryManagerException
	{
		int indice_state = GameState.getIndiceMemoryManager(state.getReadOnly());
		/**
		 * S'il est déjà détruit, on lève une exception
		 */
		if(indice_state >= firstAvailable[id_astar])
			throw new MemoryManagerException();

		// On inverse dans le Vector les deux gamestates,
		// de manière à avoir toujours un Vector trié.
		firstAvailable[id_astar]--;
		
		GameState<RobotChrono,ReadWrite> tmp1 = gamestates_list[id_astar][indice_state];
		GameState<RobotChrono,ReadWrite> tmp2 = gamestates_list[id_astar][firstAvailable[id_astar]];

		GameState.setIndiceMemoryManager(tmp1, firstAvailable[id_astar]);
		GameState.setIndiceMemoryManager(tmp2, indice_state);

		gamestates_list[id_astar][firstAvailable[id_astar]] = tmp1;
		gamestates_list[id_astar][indice_state] = tmp2;
	}
	
	/**
	 * Signale que tous les gamestates sont disponibles. Très rapide.
	 * @param id_astar
	 */
	public void empty(int id_astar)
	{
		firstAvailable[id_astar] = 0;
	}
	
	/**
	 * Utilisé par les tests
	 */
	public boolean isMemoryManagerEmpty(int id_astar)
	{
		return firstAvailable[id_astar] == 0;
	}
	
}
