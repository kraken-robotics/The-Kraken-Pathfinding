package enums;

/**
 * Valeurs des sleeps utilisés pour les actions.
 * @author pf
 *
 */

public enum SleepValues {

	SLEEP_POSER_TAPIS(200),
	SLEEP_LEVER_TAPIS(100);
	
	public final int duree;
	
	private SleepValues(int duree)
	{
		this.duree = duree;
	}
	
}
