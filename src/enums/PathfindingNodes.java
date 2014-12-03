package enums;

import smartMath.Vec2;

public enum PathfindingNodes {

	COIN1(new Vec2(700, 1250)),
	COIN2(new Vec2(-700, 1250)),
	COIN3(new Vec2(1000, 1300)),
	COIN4(new Vec2(-1000, 1350)),
	COIN5(new Vec2(1000, 700)),
	COIN6(new Vec2(-1000, 700));
	
	// TODO
	// script, au milieu, ...
	
	
	private Vec2 coordonnees;
	
	private PathfindingNodes(Vec2 coordonnees)
	{
		this.coordonnees = coordonnees;
	}
	
	public Vec2 getCoordonnees()
	{
		return coordonnees;
	}
	
}
