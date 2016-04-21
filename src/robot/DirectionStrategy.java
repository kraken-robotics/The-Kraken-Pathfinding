package robot;

/**
 * Énumération des différentes stratégies de déplacement pour les trajectoires courbes.
 * @author pf
 *
 */

public enum DirectionStrategy
{
	FASTEST(true, true, true), // faire au plus vite
	FORCE_BACK_MOTION(false, false, true), // forcer la marche arrière
	FORCE_FORWARD_MOTION(false, true, false); // forcer la marche avant	
	
	public final boolean pointRebroussementPossible, marcheAvantPossible, marcheArrierePossible;

	public static final DirectionStrategy defaultStrategy = FASTEST;
	
	public boolean isPossible(boolean marcheAvant)
	{
		if(marcheAvant)
			return marcheAvantPossible;
		else
			return marcheArrierePossible;
	}
	
	private DirectionStrategy(boolean pointRebroussementPossible, boolean marcheAvantPossible, boolean marcheArrierePossible)
	{
		this.pointRebroussementPossible = pointRebroussementPossible;
		this.marcheAvantPossible = marcheAvantPossible;
		this.marcheArrierePossible = marcheArrierePossible;
	}
	
}
