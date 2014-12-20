package utils;

/**
 * Classe qui fournie juste un sleep sans try/catch
 * NE PAS UTILISER CETTE MÉTHODE DANS UN SCRIPT!
 * @author pf
 *
 */

public class Sleep {

	// Constructeur privé car cette classe n'a qu'une méthode statique
	private Sleep()
	{
	}
	
	public static void sleep(long ms)
	{
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
