package pathfinding.thetastar;

/**
 * Différents rayons de courbure utilisés dans le lissage
 * Contient aussi la ligne droite (rayon de courbure infini) et le rebroussement.
 * @author pf
 *
 */

public enum RayonCourbure {

	EXEMPLE_1(100, 10),
	EXEMPLE_2(300, 20),
	LIGNE_DROITE(-1, 2),
	REBROUSSEMENT(-1, 0);
	
	public final int rayon;
	public final int rayonAuCarre;
	public final int PWMTranslation;
	public final double angleEntreDeuxOmbres;
	public final int nbOmbresMax;
	public static final int length = values().length;
	
	private RayonCourbure(int rayon, int PWMTranslation)
	{
		this.rayon = rayon;
		rayonAuCarre = rayon * rayon;
		angleEntreDeuxOmbres = 300. / rayon; // en considérant que le robot fasse à peu près 300 mm de long
		this.PWMTranslation = PWMTranslation;
		nbOmbresMax = (int) Math.round(Math.PI / angleEntreDeuxOmbres);
	}
	
}
