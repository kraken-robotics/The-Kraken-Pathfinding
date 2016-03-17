package pathfinding;

/**
 * Les différentes vitesses de courbure qu'on peut suivre
 * @author pf
 *
 */

public enum VitesseCourbure
{
	GAUCHE_VITE(-2),
	GAUCHE_LENTEMENT(-1),
	COURBURE_IDENTIQUE(0),
	DROITE_LENTEMENT(1),
	DROITE_VITE(2),
	REBROUSSE_AVANT(0),
	REBROUSSE_ARRIERE(0);
	
	public final int vitesse; // dérivée de la courbure, en m^-1/s
	
	public final static VitesseCourbure[] values;

	static
	{
		values = values();
	}
	
	private VitesseCourbure(int vitesse)
	{
		this.vitesse = vitesse;
	}
	
}
