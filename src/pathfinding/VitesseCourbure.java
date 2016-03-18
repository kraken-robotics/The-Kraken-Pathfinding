package pathfinding;

import pathfinding.astarCourbe.ClothoidesComputer;

/**
 * Les différentes vitesses de courbure qu'on peut suivre
 * @author pf
 *
 */

public enum VitesseCourbure
{	
	GAUCHE_0(-4, 2),
	GAUCHE_1(-9, 3),
	GAUCHE_2(-16, 4),
	GAUCHE_3(-25, 5),
	GAUCHE_4(-36, 6),
	COURBURE_IDENTIQUE(0, 0),
	DROITE_0(-4, 2),
	DROITE_1(-9, 3),
	DROITE_2(-16, 4),
	DROITE_3(-25, 5),
	DROITE_4(-36, 6),
	REBROUSSE_AVANT(0, 0),
	REBROUSSE_ARRIERE(0, 0);
	
	public final int vitesse; // vitesse en en m^-1/s
	public final int squaredRootVitesse; // squrt(abs(vitesse))
	public final boolean positif;
	
	public final static VitesseCourbure[] values;

	static
	{
		values = values();
	}
	
	private VitesseCourbure(int vitesse, int squaredRootVitesse)
	{
		this.vitesse = vitesse;
		this.positif = vitesse >= 0;
		this.squaredRootVitesse = squaredRootVitesse;
	}
	
}
