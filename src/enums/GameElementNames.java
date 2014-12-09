package enums;

/**
 * Enumérations contenant tous les éléments de jeux
 */

public enum GameElementNames {
	PLOT_1(GameElementType.PLOT),
	PLOT_2(GameElementType.PLOT),
	PLOT_3(GameElementType.PLOT),
	PLOT_4(GameElementType.PLOT),
	PLOT_5(GameElementType.PLOT),
	PLOT_6(GameElementType.PLOT),
	PLOT_7(GameElementType.PLOT),
	PLOT_8(GameElementType.PLOT),
	CLAP_1(GameElementType.CLAP),
	CLAP_2(GameElementType.CLAP),
	CLAP_3(GameElementType.CLAP),
	VERRE_1(GameElementType.VERRE),
	VERRE_2(GameElementType.VERRE),
	VERRE_3(GameElementType.VERRE),
	VERRE_4(GameElementType.VERRE),
	VERRE_5(GameElementType.VERRE),
	DISTRIB_1(GameElementType.DISTRIBUTEUR),
	DISTRIB_2(GameElementType.DISTRIBUTEUR),
	DISTRIB_3(GameElementType.DISTRIBUTEUR),
	DISTRIB_4(GameElementType.DISTRIBUTEUR);
	
	GameElementType type;
	
	private GameElementNames(GameElementType type)
	{
		this.type = type;
	}
	
	public GameElementType getType()
	{
		return type;
	}
	
}
