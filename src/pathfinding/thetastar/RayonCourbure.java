package pathfinding.thetastar;

import robot.Speed;

/**
 * Différents rayons de courbure utilisés dans le lissage
 * @author pf
 *
 */

public enum RayonCourbure {

	EXEMPLE_1(100, Speed.BETWEEN_SCRIPTS),
	EXEMPLE_2(300, Speed.BETWEEN_SCRIPTS),
	LIGNE_DROITE(-1, Speed.INTO_WALL);
	
	public final int rayon;
	public final Speed vitesseMax;
	
	private RayonCourbure(int rayon, Speed vitesseMax)
	{
		this.rayon = rayon;
		this.vitesseMax = vitesseMax;
	}
	
}
