package pathfinding;

/**
 * Les différentes vitesses de courbure qu'on peut suivre
 * @author pf
 *
 */

public enum VitesseCourbure
{
	GAUCHE_VITE(-2, false),
	GAUCHE_LENTEMENT(-1, false),
	COURBURE_IDENTIQUE(0, false),
	DROITE_LENTEMENT(1, false),
	DROITE_VITE(2, false),
	REBROUSSE_AVANT(0, true),
	REBROUSSE_ARRIERE(0, true);
	
	public final int vitesse;
	public final boolean faisableALArret;
	public final static VitesseCourbure[] values;
	
	static
	{
		values = values();
	}
	
	private VitesseCourbure(int vitesse, boolean faisableALArret)
	{
		this.vitesse = vitesse;
		this.faisableALArret = faisableALArret;
	}
	
}
