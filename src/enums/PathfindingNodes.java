package enums;

import smartMath.Vec2;

/**
 * Les noeuds utilisés par la recherche de chemin
 * @author pf
 *
 */

// TODO: tester et ajuster

public enum PathfindingNodes {
	DEVANT_DEPART_DROITE(new Vec2(700, 1100)),
	HAUT_DROITE(new Vec2(1000, 1600)),
	BAS_DROITE(new Vec2(800, 450)),
	COTE_MARCHE_DROITE(new Vec2(830, 1600)),
	DEVANT_DEPART_GAUCHE(new Vec2(-700, 1100)),
	HAUT_GAUCHE(new Vec2(-1000, 1600)),
	BAS_GAUCHE(new Vec2(-800, 450)),
	COTE_MARCHE_GAUCHE(new Vec2(-830, 1600)),
	NODE_TAPIS(new Vec2(250, 1100)),
	BAS(new Vec2(0, 500));
	
	private Vec2 coordonnees;
	
	// Permet surtout de savoir si on n'emprunte jamais certains noeuds...
	private int use;
	
	private PathfindingNodes(Vec2 coordonnees)
	{
		this.coordonnees = coordonnees;
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
