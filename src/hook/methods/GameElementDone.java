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
	private Tribool done;
	
	public GameElementDone(GameElement o, Tribool done)
	{
		this.o = o;
		this.done = done;
	}
	
	@Override
	public boolean execute()
	{
		System.out.println(o.getName()+" is done by hook");
		o.setDone(done);
		return false;
	}

}
