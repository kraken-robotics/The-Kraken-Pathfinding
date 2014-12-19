package strategie;

import java.util.ArrayList;
import java.util.Vector;

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

	private ArrayList<GameState<RobotChrono>> gamestate = new ArrayList<GameState<RobotChrono>>();
	private Vector<Boolean> available = new Vector<Boolean>();

	private Log log;
	private GameState<RobotChrono> model;
	
	private static final int nb_instances = 100;
	
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
				available.add(Boolean.TRUE);
				gamestate.add(model.cloneGameState(gamestate.size()));
			}
		} catch (FinMatchException e) {
			e.printStackTrace();
		}
		log.debug("Instanciation finie", this);
	}
	
	public GameState<RobotChrono> getNewGameState() throws FinMatchException
	{
		int indice = available.indexOf(Boolean.TRUE);
		if(indice == -1) // pas de gamestate disponible
		{
			available.add(Boolean.FALSE);
			if(available.size() > 1000)
			{
				int z = 0;
				z = 1/z;
			}
			GameState<RobotChrono> out = model.cloneGameState(gamestate.size());
			gamestate.add(out);
			return out;
		}
		else // on a un robot disponible
		{
//			log.debug("Réutilisation de "+indice, this);
			available.setElementAt(Boolean.FALSE, indice);
			return gamestate.get(indice);
		}
	}
	
	public GameState<RobotChrono> destroyGameState(GameState<RobotChrono> state)
	{
		if(available.get(state.getIndiceMemoryManager()) == Boolean.TRUE)
			log.warning("Objet déjà détruit!", this);
		available.setElementAt(Boolean.TRUE, state.getIndiceMemoryManager());
		return null;
	}
	
	/**
	 * Utilisé par les tests
	 */
	public boolean isMemoryManagerEmpty()
	{
		return available.indexOf(Boolean.FALSE) == -1;
	}
	
}
