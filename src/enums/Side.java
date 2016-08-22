package enums;

/**
 * Enum pour distinguer les cotés du robot
 * @author karton, pf
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
