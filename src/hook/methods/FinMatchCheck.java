package hook.methods;

import exceptions.FinMatchException;
import exceptions.FinMatchFuturException;
import hook.Executable;

/**
 * Lève une exception une fois le match fini
 * @author pf
 *
 */

public class FinMatchCheck implements Executable {
	
	private boolean chrono = false;
	
	public FinMatchCheck(boolean chrono)
	{
		 this.chrono = chrono;
	}

	@Override
	public void execute() throws FinMatchException
	{
		if(chrono)
			throw new FinMatchException();
		else
			throw new FinMatchFuturException();
	}

}
