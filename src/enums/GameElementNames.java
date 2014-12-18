package enums;

import smartMath.Vec2;

/**
 * Enumérations contenant tous les éléments de jeux
 */

public enum GameElementNames {
	PLOT_1(GameElementType.PLOT, new Vec2(1410, 150), 30),
	PLOT_2(GameElementType.PLOT, new Vec2(1410, 250), 30),
	PLOT_3(GameElementType.PLOT, new Vec2(1410, 1300), 30),
	PLOT_4(GameElementType.PLOT, new Vec2(650, 1300), 30),
	PLOT_5(GameElementType.PLOT, new Vec2(650, 1400), 30),
	PLOT_6(GameElementType.PLOT, new Vec2(200, 600), 30),
	PLOT_7(GameElementType.PLOT, new Vec2(630, 645), 30),
	PLOT_8(GameElementType.PLOT, new Vec2(400, 230), 30),
	CLAP_1(GameElementType.CLAP, new Vec2(650, 0), -1),
	CLAP_2(GameElementType.CLAP, new Vec2(-950, 0), -1),
	CLAP_3(GameElementType.CLAP, new Vec2(1250, 0), -1),
	VERRE_1(GameElementType.VERRE, new Vec2(-1250, 250), 50),
	VERRE_2(GameElementType.VERRE, new Vec2(1250, 250), 50),
	VERRE_3(GameElementType.VERRE, new Vec2(-590, 1200), 50),
	VERRE_4(GameElementType.VERRE, new Vec2(590, 1200), 50),
	VERRE_5(GameElementType.VERRE, new Vec2(0, 350), 50),
	DISTRIB_1(GameElementType.DISTRIBUTEUR, new Vec2(900, 1950), 25),
	DISTRIB_2(GameElementType.DISTRIBUTEUR, new Vec2(1200, 1950), 25),
	DISTRIB_3(GameElementType.DISTRIBUTEUR, new Vec2(-900, 1950), 25),
	DISTRIB_4(GameElementType.DISTRIBUTEUR, new Vec2(-1200, 1950), 25);
	
	private GameElementType type;
	private int radius;
	private Vec2 position;

	public Vec2 getPosition()
	{
		return position;
	}
	
	public int getRadius()
	{
		return radius;
	}
	
	private GameElementNames(GameElementType type, Vec2 position, int radius)
	{
		this.position = position;
		this.radius = radius;
		this.type = type;
	}
	
	public GameElementType getType()
	{
		return type;
	}
	
}
