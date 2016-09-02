package threads;

import container.Container;
import exceptions.ContainerException;


/**
 * Thread qui sera exécuté à la fin du programme
 * @author pf
 *
 */

public class ThreadExit extends Thread
{
	protected Container container;
	private static ThreadExit instance = null;
	
	public static ThreadExit getInstance()
	{
		return instance;
	}
	
	public static ThreadExit makeInstance(Container container)
	{
		return instance = new ThreadExit(container);
	}

	private ThreadExit(Container container)
	{
		this.container = container;
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName("ThreadRobotExit");
		try {
			container.destructor();
		} catch (ContainerException | InterruptedException e) {
			System.out.println(e);
		}
	}
}