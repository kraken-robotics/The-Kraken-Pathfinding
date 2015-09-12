package robot;

public enum VitesseCourbure
{
	GAUCHE_VITE(-2),
	GAUCHE_LENTEMENT(-1),
	COURBURE_IDENTIQUE(0),
	DROITE_LENTEMENT(1),
	DROITE_VITE(2);
	
	public final int vitesse;
	
	private VitesseCourbure(int vitesse)
	{
		this.vitesse = vitesse;
	}
	
}
