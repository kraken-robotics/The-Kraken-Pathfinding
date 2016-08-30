package threads;

import container.Container;


/**
 * Thread qui sera exécuté à la fin de l'exécution
 * @author pf
 *
 */

public class ThreadExit extends Thread
{
	protected Container container;
	
	public ThreadExit(Container container)
	{
		this.container = container;
	}
	
	@Override
	public void run()
	{
		Thread.currentThread().setName("ThreadExit");
		container.destructor();
	}
}