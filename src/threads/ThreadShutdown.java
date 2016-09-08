package threads;

import container.Container;
import exceptions.ContainerException;


/**
 * Thread qui sera exécuté à la fin du programme
 * @author pf
 *
 */

public class ThreadShutdown extends Thread
{
	protected Container container;
	private static ThreadShutdown instance = null;
	
	public static ThreadShutdown getInstance()
	{
		return instance;
	}
	
	public static ThreadShutdown makeInstance(Container container)
	{
		return instance = new ThreadShutdown(container);
	}

	private ThreadShutdown(Container container)
	{
		this.container = container;
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName("ThreadRobotShutdown");
		try {
			container.destructor(false);
		} catch (ContainerException | InterruptedException e) {
			System.out.println(e);
		}
	}
}