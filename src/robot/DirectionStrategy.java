package robot;

/**
 * Énumération des différentes stratégies de déplacement pour les trajectoires courbes.
 * @author pf
 *
 */

public enum DirectionStrategy
{
	FASTEST(true), // faire au plus vite
	FORCE_BACK_MOTION(false), // forcer la marche arrière
	FORCE_FORWARD_MOTION(false); // forcer la marche avant	
	
	public final boolean pointRebroussementPossible;
	
	// DEPENDS ON RULES
	public static final DirectionStrategy defaultStrategy = FASTEST;
	
	private DirectionStrategy(boolean pointRebroussementPossible)
	{
		this.pointRebroussementPossible = pointRebroussementPossible;
	}
	
}
