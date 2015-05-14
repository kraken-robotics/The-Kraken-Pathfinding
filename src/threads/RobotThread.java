package threads;

/**
 * Classe abstraite des threads
 * @author pf,marsu
 *
 */

public abstract class RobotThread extends Thread {

	// Permet d'arrêter tous les threads d'un coup
	protected static boolean stopThreads = false;
	public static boolean finMatch = false;
		
	/**
	 * Arrête tous les threads.
	 */
	public static void stopAllThread()
	{
		stopThreads = true;
	}

}

