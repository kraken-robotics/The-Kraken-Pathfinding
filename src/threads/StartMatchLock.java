package threads;

/**
 * Juste un singleton qui sert à démarrer les threads
 * @author pf
 *
 */

public class StartMatchLock
{
	private static final StartMatchLock instance = new StartMatchLock();
	
	private StartMatchLock()
	{}
	
	public static StartMatchLock getInstance()
	{
		return instance;
	}
	
}
