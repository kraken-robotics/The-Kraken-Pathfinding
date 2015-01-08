package astar;

import java.util.Vector;

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

	private static final int nb_instances = 100;

	private Vector<GameState<RobotChrono>> gamestates_list = new Vector<GameState<RobotChrono>>();
	protected Log log;
	private GameState<RobotChrono> model;
	
	// gamestate est triés: avant firstAvailable, les gamestate sont indisponibles, après, ils sont disponibles
	private int firstAvailable = 0;
	
	@Override
	public void updateConfig() {
	}

	public MemoryManager(Log log, Config config, GameState<RobotReal> realstate)
	{	
		this.log = log;

		try {
			this.model = realstate.cloneGameState();
			// on prépare déjà des gamestates
			log.debug("Instanciation de "+nb_instances+" GameState<RobotChrono>", this);
		
			for(int i = 0; i < nb_instances; i++)
			{
				gamestates_list.add(model.cloneGameState(gamestates_list.size()));
			}
		} catch (FinMatchException e) {
			// Impossible
			e.printStackTrace();
		}
		log.debug("Instanciation finie", this);
		updateConfig();
	}
	
	public GameState<RobotChrono> getNewGameState() throws FinMatchException
	{
		GameState<RobotChrono> out;
		try {
			out = gamestates_list.get(firstAvailable);
			firstAvailable++;
			return out;
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			out = model.cloneGameState(firstAvailable);
			gamestates_list.add(out);
			firstAvailable++;
			return out;
		}
	}
	
	public void destroyGameState(GameState<RobotChrono> state) throws MemoryManagerException
	{
		int indice_state = state.getIndiceMemoryManager();
		if(indice_state >= firstAvailable)
			throw new MemoryManagerException();

		// On inverse dans le Vector les deux gamestates,
		// de manière à avoir toujours un Vector trié.
		firstAvailable--;
		GameState<RobotChrono> tmp1 = gamestates_list.get(indice_state);
		GameState<RobotChrono> tmp2 = gamestates_list.get(firstAvailable);

		tmp1.setIndiceMemoryManager(firstAvailable);
		tmp2.setIndiceMemoryManager(indice_state);

		gamestates_list.setElementAt(tmp1, firstAvailable);
		gamestates_list.setElementAt(tmp2, indice_state);
	}
	
	/**
	 * Utilisé par les tests
	 */
	public boolean isMemoryManagerEmpty()
	{
		return firstAvailable == 0;
	}
	
}
