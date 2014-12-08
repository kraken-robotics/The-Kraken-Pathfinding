package enums;

import smartMath.Vec2;

/**
 * Les noeuds utilisés par la recherche de chemin
 * @author pf
 *
 */

// TODO: tester et ajuster

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
	BAS(new Vec2(0, 500), false);
	
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
