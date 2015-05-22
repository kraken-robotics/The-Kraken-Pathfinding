package threads;

/**
 * Une petite surcharge de thread afin d'implémenter un arrêt propre.
 * @author pf
 *
 */

public class ThreadAvecStop extends Thread
{
	protected volatile boolean finThread;
	
	public void setFinThread()
	{
		finThread = true;
	}
}
