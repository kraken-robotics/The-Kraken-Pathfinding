package table;

import obstacles.types.ObstacleCircular;
import utils.Vec2;
import utils.permissions.ReadOnly;

/**
 * Enumérations contenant tous les éléments de jeux
 * @author pf
 *
 */

// DEPENDS_ON_RULES

public enum GameElementNames {
	POISSONS(GameElementType.POISSONS, new Vec2<ReadOnly>(1410, 150), -1),
	TAS_SABLE_PROCHE(GameElementType.SABLE, new Vec2<ReadOnly>(850, 1100), 30),
	TAS_SABLE_LOIN(GameElementType.SABLE, new Vec2<ReadOnly>(-850, 1100), 30),
	DRAPEAU_1(GameElementType.DRAPEAU, new Vec2<ReadOnly>(900, 1950), -1),
	DRAPEAU_2(GameElementType.DRAPEAU, new Vec2<ReadOnly>(1200, 1950), -1),
	COQUILLAGE_1(),
	COQUILLAGE_2(),
	COQUILLAGE_3(),
	COQUILLAGE_4(),
	COQUILLAGE_5(),
	COQUILLAGE_6(),
	COQUILLAGE_7(),
	COQUILLAGE_8(),
	COQUILLAGE_9(),
	COQUILLAGE_10(),
	COQUILLAGE_ROCHER_DROITE_SOMMET(),
	COQUILLAGE_ROCHER_DROITE_INTERIEUR(),
	COQUILLAGE_ROCHER_DROITE_EXTERIEUR(),
	COQUILLAGE_ROCHER_GAUCHE_SOMMET(),
	COQUILLAGE_ROCHER_GAUCHE_INTERIEUR(),
	COQUILLAGE_ROCHER_GAUCHE_EXTERIEUR();

	private GameElementType type;
	private ObstacleCircular obstacle;

	public static final GameElementNames[] values;
	public static final int valuesLength;
	
	static
	{
		values = values();
		valuesLength = values.length;
	}
	
	public ObstacleCircular getObstacle()
	{
		return obstacle;
	}

	private GameElementNames()
	{}

	private GameElementNames(GameElementType type, Vec2<ReadOnly> position, int radius)
	{
		obstacle = new ObstacleCircular(position, radius);
		this.type = type;
	}
	
	public GameElementType getType()
	{
		return type;
	}

	public void set(GameElementType type, ObstacleCircular obs)
	{
		obstacle = obs;
		this.type = type;
	}
	
}
