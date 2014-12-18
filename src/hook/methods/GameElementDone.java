package hook.methods;

import pathfinding.GridSpace;
import enums.GameElementNames;
import enums.Tribool;
import hook.Executable;

/**
 * 
 * @author pf
 *
 */

public class GameElementDone implements Executable {

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
	public boolean execute()
	{
		gridspace.setDone(element, done);
		return false;
	}

}
