package enums;

/**
 * Utilitaire pour ditinguer les cotés, soit ceux du robot, soit d'autre chose.
 * @author karton
 *
 */
public enum Side
{
	// coté gauche
	LEFT,
		
	// le milieu
	MIDDLE,
	
	// coté droit
	RIGHT;

	/**
	 * Fournit le symétrique.
	 * LEFT renvoie RIGHT
	 * MIDDLE renvoie MIDDLE
	 * RIGHT renvoie LEFT
	 * @return
	 */
	public Side getSymmetric()
	{
		return Side.values()[2-ordinal()];
	}
}
