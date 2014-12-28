package hook.methods;

import exceptions.FinMatchException;
import hook.Executable;

/**
 * Lève une exception une fois le match fini
 * @author pf
 *
 */

public class FinMatchCheck implements Executable {

	@Override
	public void execute() throws FinMatchException
	{
		throw new FinMatchException();
	}

}
