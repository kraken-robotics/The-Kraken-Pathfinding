package threads;

import container.Container;
import exceptions.ContainerException;


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
		try {
			container.destructor();
		} catch (ContainerException | InterruptedException e) {
			System.out.println(e);
		}
	}
}