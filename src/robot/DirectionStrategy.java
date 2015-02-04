package robot;

/**
 * Utilisé par Locomotion
 * @author pf
 *
 */

public enum DirectionStrategy
{
	FASTEST, // faire au plus vite
	FORCE_BACK_MOTION, // forcer la marche arrière
	FORCE_FORWARD_MOTION; // forcer la marche avant	
	
	// DEPENDS ON RULES
	public static DirectionStrategy getDefaultStrategy()
	{
		return FASTEST;
	}
	
}
