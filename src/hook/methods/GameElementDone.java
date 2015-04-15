package hook.methods;

import robot.RobotChrono;
import strategie.GameState;
import table.GameElementNames;
import vec2.ReadWrite;
import enums.Tribool;
import hook.Executable;

/**
 * Modifie l'état des éléments de jeux
 * @author pf
 *
 */

public class GameElementDone implements Executable
{

	private GameState<?, ReadWrite> state;
	private Tribool done;
	private GameElementNames element;
	
	public GameElementDone(GameState<?, ReadWrite> state, GameElementNames element, Tribool done)
	{
		this.state = state;
		this.done = done;
		this.element = element;
	}
	
	@Override
	public void execute()
	{
		// on ne peut faire qu'augmenter l'état d'un élément de jeu.
		// c'est-à-dire qu'on peut passer de FALSE à MAYBE et TRUE
		// et de MAYBE à TRUE.
		// Les autres transitions sont interdites (en particulier passer de TRUE à MAYBE...)
		if(GameState.isDone(state.getReadOnly(), element).getHash() < done.getHash())
			GameState.setDone(state, element, done);
	}

	@Override
	public void updateGameState(GameState<RobotChrono,ReadWrite> state)
	{
		this.state = state;
	}

}
