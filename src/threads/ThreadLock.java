package threads;

/**
 * Juste un singleton qui sert à démarrer les threads
 * @author pf
 *
 */

public class ThreadLock
{
	private static final ThreadLock instance = new ThreadLock();
	
	private ThreadLock()
	{}
	
	public static ThreadLock getInstance()
	{
		return instance;
	}
	
}
