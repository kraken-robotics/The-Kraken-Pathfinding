package hook.methods;

import obstacles.GameElement;
import enums.Tribool;
import hook.Executable;

/**
 * 
 * @author pf
 *
 */

public class GameElementDone implements Executable {

	private GameElement o;
	
	public GameElementDone(GameElement o)
	{
		this.o = o;
	}
	
	@Override
	public boolean execute()
	{
		o.setDone(Tribool.TRUE);
		return false;
	}

}
