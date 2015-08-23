package pathfinding;

public enum RayonCourbure {

	EXEMPLE_1(100),
	EXEMPLE_2(300),
	LIGNE_DROITE(-1);
	
	public final int rayon;
	
	private RayonCourbure(int rayon)
	{
		this.rayon = rayon;
	}
	
}
