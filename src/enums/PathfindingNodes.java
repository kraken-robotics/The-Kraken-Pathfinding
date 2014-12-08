package enums;

import smartMath.Vec2;

/**
 * Les noeuds utilisés par la recherche de chemin
 * @author pf
 *
 */

public enum PathfindingNodes {
	DEVANT_DEPART_DROITE(new Vec2(700, 1100), false),
	HAUT_DROITE(new Vec2(1000, 1600), false),
	BAS_DROITE(new Vec2(800, 450), false),
	COTE_MARCHE_DROITE(new Vec2(830, 1600), false),
	DEVANT_DEPART_GAUCHE(new Vec2(-700, 1100), false),
	HAUT_GAUCHE(new Vec2(-1000, 1600), false),
	BAS_GAUCHE(new Vec2(-800, 450), false),
	COTE_MARCHE_GAUCHE(new Vec2(-830, 1600), false),
	NODE_TAPIS(new Vec2(250, 1100), false),
	CLAP_GAUCHE(new Vec2(1150, 300), false),
	CLAP_DROIT(new Vec2(-1150, 300), false),
	BAS(new Vec2(0, 500), false),

	// Les points de secours ne doivent pas être des points d'entrée de scripts.
	SECOURS_1(new Vec2(800, 1300), true),
	SECOURS_2(new Vec2(-800, 1300), true),
	SECOURS_3(new Vec2(0, 800), true),
	SECOURS_4(new Vec2(820, 880), true),
	SECOURS_5(new Vec2(-820, 880), true),
	SECOURS_6(new Vec2(425, 520), true),
	SECOURS_7(new Vec2(-425, 520), true);
	
	private Vec2 coordonnees;
	private boolean emergency_point;
	
	// Permet surtout de savoir si on n'emprunte jamais certains noeuds...
	private int use;
	
	private PathfindingNodes(Vec2 coordonnees, boolean emergency_point)
	{
		this.coordonnees = coordonnees;
		this.emergency_point = emergency_point;
	}
	
	public boolean is_an_emergency_point()
	{
		return emergency_point;
	}
	
	public Vec2 getCoordonnees()
	{
		return coordonnees;
	}
	
	public void incrementUse()
	{
		use++;
	}
	
	public int getNbUse()
	{
		return use;
	}
	
}
