package enums;

import smartMath.Vec2;

/**
 * Les noeuds utilisés par la recherche de chemin
 * @author pf
 *
 */

// TODO: tester et ajuster

public enum PathfindingNodes {

	COIN_1(new Vec2(700, 1250)), // marche
	COIN_2(new Vec2(-700, 1250)),
	COIN_3(new Vec2(1000, 1300)), // zones de départ haut
	COIN_4(new Vec2(-1000, 1350)),
	COIN_5(new Vec2(1000, 700)), // zones de départ bas
	COIN_6(new Vec2(-1000, 700)),
	
	// vraiment nécessaires?...
	LONGE_1(new Vec2(0, 1200)), // longe les marches
	LONGE_2(new Vec2(900, 1000)), // longes les zones de départ
	LONGE_3(new Vec2(-900, 1000)),

	SCRIPT_PLOT_1(new Vec2(800, 650)), // plots
	SCRIPT_PLOT_2(new Vec2(350, 600)),
	SCRIPT_PLOT_3(new Vec2(-800, 650)),
	SCRIPT_PLOT_4(new Vec2(-350, 650)),
	SCRIPT_PLOT_5(new Vec2(500, 200)),
	SCRIPT_PLOT_6(new Vec2(-500, 200)),
	SCRIPT_PLOT_7(new Vec2(1000, 250)),
	SCRIPT_PLOT_8(new Vec2(-1000, 250)),
	SCRIPT_PLOT_9(new Vec2(1000, 1700)),
	SCRIPT_PLOT_10(new Vec2(-1000, 1700)),
	
	SCRIPT_VERRE_1(new Vec2(750, 1100)), // verres
	SCRIPT_VERRE_2(new Vec2(-750, 1100)),
	
	SCRIPT_TAPIS_1(new Vec2(250, 1300)), // tapis
	SCRIPT_TAPIS_2(new Vec2(-250, 1300)),
	
	SCRIPT_CLAP_1(new Vec2(1200, 300)), // clap
	SCRIPT_CLAP_2(new Vec2(-900, 300)),
	SCRIPT_CLAP_3(new Vec2(500, 300)),
	
	MILIEU_1(new Vec2(0, 600)), // juste des points au milieu
	MILIEU_2(new Vec2(0, 900));
	
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
