package hook.methods;

import java.util.ArrayList;

import pathfinding.GameState;
import permissions.ReadWrite;
import robot.RobotChrono;
import table.GameElementNames;
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
		if(state.table.isDone(element).hash < done.hash)
			state.table.setDone(element, done);
	}

	@Override
	public void updateGameState(GameState<RobotChrono,ReadWrite> state)
	{
		this.state = state;
	}
	
	@Override
	public ArrayList<String> toSerial()
	{
		ArrayList<String> out = new ArrayList<String>();
		out.add("tbl");
		out.add(String.valueOf(element.ordinal()));
//		out.add(String.valueOf(done.ordinal()));
		return out;
	}


}
