package hook.methods;

import pathfinding.GridSpace;
import robot.RobotChrono;
import strategie.GameState;
import enums.GameElementNames;
import enums.Tribool;
import hook.Executable;

/**
 * Modifie l'état des éléments de jeux
 * @author pf
 *
 */

public class GameElementDone implements Executable
{

	private GridSpace gridspace;
	private Tribool done;
	private GameElementNames element;
	
	public GameElementDone(GridSpace gridspace, GameElementNames element, Tribool done)
	{
		this.gridspace = gridspace;
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
		if(gridspace.isDone(element).getHash() < done.getHash())
			gridspace.setDone(element, done);
	}

	@Override
	public void updateGameState(GameState<RobotChrono> state)
	{
		gridspace = state.gridspace;
	}

}
