package threads;

/**
 * Juste un singleton qui sert au démarrage du match
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
