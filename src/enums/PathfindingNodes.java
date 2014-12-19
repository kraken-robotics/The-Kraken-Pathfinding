package enums;

import pathfinding.Arc;
import smartMath.Vec2;

/**
 * Les noeuds utilisés par la recherche de chemin
 * @author pf
 *
 */

public enum PathfindingNodes implements Arc {
	DEVANT_DEPART_DROITE(new Vec2(700, 1100)),
	HAUT_DROITE(new Vec2(1000, 1600)),
	BAS_DROITE(new Vec2(800, 450)),
	COTE_MARCHE_DROITE(new Vec2(830, 1600)),
	DEVANT_DEPART_GAUCHE(new Vec2(-700, 1100)),
	HAUT_GAUCHE(new Vec2(-1000, 1600)),
	BAS_GAUCHE(new Vec2(-800, 450)),
	COTE_MARCHE_GAUCHE(new Vec2(-830, 1600)),
	NODE_TAPIS(new Vec2(250, 1100)),
	CLAP_GAUCHE(new Vec2(-1050, 300)), // les claps ne sont pas symétriques, c'est normal
	CLAP_DROIT(new Vec2(1150, 300)),
	BAS(new Vec2(0, 500)),

	SORTIE_CLAP_GAUCHE(CLAP_GAUCHE),
	SORTIE_CLAP_DROIT(CLAP_DROIT),
	SORTIE_TAPIS(NODE_TAPIS),
	
	SECOURS_1(new Vec2(800, 1300)),
	SECOURS_2(new Vec2(-800, 1300)),
	SECOURS_3(new Vec2(0, 800)),
	SECOURS_4(new Vec2(820, 880)),
	SECOURS_5(new Vec2(-820, 880)),
	SECOURS_6(new Vec2(425, 520)),
	SECOURS_7(new Vec2(-425, 520)),
	SECOURS_8(new Vec2(575, 800)),
	SECOURS_9(new Vec2(-575, 800));
	
	// Contient les distances entre chaque point de passage
	private static double[][] distances = new double[PathfindingNodes.values().length][PathfindingNodes.values().length];

	// contient l'orientation du robot après le trajet
	private static double[][] orientations = new double[PathfindingNodes.values().length][PathfindingNodes.values().length];

	static {
		for(PathfindingNodes i : PathfindingNodes.values())
			for(PathfindingNodes j : PathfindingNodes.values())
			{
				distances[i.ordinal()][j.ordinal()] = i.getCoordonnees().distance(j.getCoordonnees());
				orientations[i.ordinal()][j.ordinal()] = Math.atan2(i.getCoordonnees().y - j.getCoordonnees().y, i.getCoordonnees().x - j.getCoordonnees().y);
			}
		
	}
	
	private Vec2 coordonnees;
	
	private PathfindingNodes(Vec2 coordonnees)
	{
		this.coordonnees = coordonnees;
	}

	private PathfindingNodes(PathfindingNodes n)
	{
		this.coordonnees = n.getCoordonnees();
	}

	public void setCoordonnees(Vec2 coordonnees)
	{
		coordonnees = coordonnees.clone();
	}

	public Vec2 getCoordonnees()
	{
		return coordonnees;
	}

	public double distanceTo(PathfindingNodes n)
	{
		return distances[ordinal()][n.ordinal()];
	}
	
	public double getOrientationFinale(PathfindingNodes n)
	{
		return orientations[ordinal()][n.ordinal()];
	}

}
