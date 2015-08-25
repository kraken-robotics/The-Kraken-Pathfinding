package pathfinding.thetastar;

/**
 * Différents rayons de courbure utilisés dans le lissage
 * @author pf
 *
 */

public enum RayonCourbure {

	EXEMPLE_1(100, 10),
	EXEMPLE_2(300, 20),
	LIGNE_DROITE(-1, 2);
	
	public final int rayon;
	public final int PWMTranslation;
	public static final int length = values().length;
	
	private RayonCourbure(int rayon, int PWMTranslation)
	{
		this.rayon = rayon;
		this.PWMTranslation = PWMTranslation;
	}
	
}
