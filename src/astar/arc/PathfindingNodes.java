package astar.arc;

import robot.Speed;
import utils.Vec2;

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
	NODE_TAPIS(new Vec2(290, 1030)), // TODO
	CLAP_GAUCHE(new Vec2(-1070, 280)), // TODO . les claps ne sont pas symétriques, c'est normal
	CLAP_DROIT(new Vec2(1250, 280)), // TODO
	CLAP_DROIT_SECOND(new Vec2(770, 280)),
	BAS(new Vec2(0, 500)),

	POINT_DEPART(new Vec2(1483, 1000)), // TODO: mesurer. Problème: dégomme le plot 3...
	
	SORTIE_ZONE_DEPART(new Vec2(983,1000)),
	SORTIE_CLAP_GAUCHE(new Vec2(-770,480)),
	SORTIE_CLAP_DROIT(new Vec2(450,480)),
	SORTIE_CLAP_DROIT_SECOND(new Vec2(170,480)),
	SORTIE_TAPIS(new Vec2(290,1030)),
	
	SECOURS_0(new Vec2(800, 600)),

	
	SECOURS_1(new Vec2(800, 1300)),
	SECOURS_2(new Vec2(-800, 1300)),
	SECOURS_3(new Vec2(0, 800)),
	SECOURS_4(new Vec2(820, 880)),
	SECOURS_5(new Vec2(-820, 880)),
	SECOURS_6(new Vec2(425, 520)),
	SECOURS_7(new Vec2(-425, 520)),
	SECOURS_8(new Vec2(575, 800)),
	SECOURS_9(new Vec2(-575, 800));
	
	public static final PathfindingNodes[] values;
	public static final int length;
	
	// Contient les distances entre chaque point de passage
	private static double[][] distances = new double[PathfindingNodes.values().length][PathfindingNodes.values().length];

	// Contient les temps de parcourt entre chaque point de passage
	private static double[][] durations = new double[PathfindingNodes.values().length][PathfindingNodes.values().length];

	// contient l'orientation du robot après le trajet
	private static double[][] orientations = new double[PathfindingNodes.values().length][PathfindingNodes.values().length];

	// le delta d'angle entre deux segments
	private static double[][][] angleWith = new double[PathfindingNodes.values().length][PathfindingNodes.values().length][PathfindingNodes.values().length];
	
	static {
		for(PathfindingNodes i : PathfindingNodes.values())
			for(PathfindingNodes j : PathfindingNodes.values())
			{
				distances[i.ordinal()][j.ordinal()] = i.getCoordonnees().distance(j.getCoordonnees());
				durations[i.ordinal()][j.ordinal()] = i.getCoordonnees().distance(j.getCoordonnees())*Speed.BETWEEN_SCRIPTS.invertedTranslationnalSpeed;
				orientations[i.ordinal()][j.ordinal()] = Math.atan2(i.getCoordonnees().y - j.getCoordonnees().y, i.getCoordonnees().x - j.getCoordonnees().y);
			}
		for(PathfindingNodes i : PathfindingNodes.values())
			for(PathfindingNodes j : PathfindingNodes.values())
				for(PathfindingNodes k : PathfindingNodes.values())
				{
					double delta = orientations[i.ordinal()][j.ordinal()]-orientations[j.ordinal()][k.ordinal()] % (2*Math.PI);
					delta = Math.abs(delta);
					if(delta > Math.PI)
						delta = 2*(float)Math.PI - delta;
					angleWith[i.ordinal()][j.ordinal()][k.ordinal()] = delta*Speed.BETWEEN_SCRIPTS.invertedRotationnalSpeed;
				}
		values = values();
		length = values.length;
	}
	
	private Vec2 coordonnees;
	
	private PathfindingNodes(Vec2 coordonnees)
	{
		this.coordonnees = coordonnees;
	}

	public Vec2 getCoordonnees()
	{
		return coordonnees;
	}

	public double distanceTo(PathfindingNodes n)
	{
		return distances[ordinal()][n.ordinal()];
	}

	public double timeTo(PathfindingNodes n)
	{
		return durations[ordinal()][n.ordinal()];
	}

	public double getOrientationFinale(PathfindingNodes n)
	{
		return orientations[ordinal()][n.ordinal()];
	}

	public double angleWith(PathfindingNodes pointPrecedent, PathfindingNodes pointSuivant) {
		return angleWith[pointPrecedent.ordinal()][ordinal()][pointSuivant.ordinal()];
	}
	
}
