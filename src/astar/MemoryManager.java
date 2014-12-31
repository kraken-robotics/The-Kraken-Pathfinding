package astar;

import java.util.Vector;

import container.Service;
import exceptions.FinMatchException;
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

	private Vector<GameState<RobotChrono>> gamestate = new Vector<GameState<RobotChrono>>();
	private Log log;
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
			// on prépare déjà des gamestate
			log.debug("Instanciation de "+nb_instances+" GameState<RobotChrono>", this);
		
			for(int i = 0; i < nb_instances; i++)
			{
				gamestate.add(model.cloneGameState(gamestate.size()));
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
			out = gamestate.get(firstAvailable);
			firstAvailable++;
			return out;
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			out = model.cloneGameState(firstAvailable);
			gamestate.add(out);
			firstAvailable++;
			return out;
		}
	}
	
	public GameState<RobotChrono> destroyGameState(GameState<RobotChrono> state)
	{
		int indice_state = state.getIndiceMemoryManager();
		if(indice_state >= firstAvailable)
			log.warning("Objet déjà détruit!", this);
//		else
//		{
			// On inverse dans le Vector les deux gamestates,
			// de manière à avoir toujours un Vector trié.
			firstAvailable--;
			GameState<RobotChrono> tmp1 = gamestate.get(indice_state);
			GameState<RobotChrono> tmp2 = gamestate.get(firstAvailable);

			tmp1.setIndiceMemoryManager(firstAvailable);
			tmp2.setIndiceMemoryManager(indice_state);

			gamestate.setElementAt(tmp1, firstAvailable);
			gamestate.setElementAt(tmp2, indice_state);
//		}
		return null;
	}
	
	/**
	 * Utilisé par les tests
	 */
	public boolean isMemoryManagerEmpty()
	{
		return firstAvailable == 0;
	}
	
}
