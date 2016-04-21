package pathfinding;

/**
 * Les différentes vitesses de courbure qu'on peut suivre
 * @author pf
 *
 */

public enum VitesseCourbure
{
	GAUCHE_0(1, 1, false),
	GAUCHE_1(4, 2, false),
//	GAUCHE_2(9, 3, false),
	GAUCHE_3(16, 4, false),
	COURBURE_IDENTIQUE(0, 0, false),
	DROITE_0(-1, 1, false),
	DROITE_1(-4, 2, false),
//	DROITE_2(-9, 3, false),
	DROITE_3(-16, 4, false),
	
	GAUCHE_0_REBROUSSE(1, 1, true),
//	GAUCHE_1_REBROUSSE(4, 2, true),
//	GAUCHE_2_REBROUSSE(9, 3, true),
	GAUCHE_3_REBROUSSE(16, 4, true),
	COURBURE_IDENTIQUE_REBROUSSE(0, 0, true),
	DROITE_0_REBROUSSE(-1, 1, true),
//	DROITE_1_REBROUSSE(-4, 2, true),
//	DROITE_2_REBROUSSE(-9, 3, true),
	DROITE_3_REBROUSSE(-16, 4, true);

	public final int vitesse; // vitesse en en m^-1/s
	public final int squaredRootVitesse; // squrt(abs(vitesse))
	public final boolean positif;
	public final boolean rebrousse;
	
	public final static VitesseCourbure[] values;

	static
	{
		values = values();
	}
	
	private VitesseCourbure(int vitesse, int squaredRootVitesse, boolean rebrousse)
	{
		this.rebrousse = rebrousse;
		this.vitesse = vitesse;
		this.positif = vitesse >= 0;
		this.squaredRootVitesse = squaredRootVitesse;
	}
	
}
