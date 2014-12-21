package enums;

/**
 * Type d'élément de jeux.
 * @author pf
 *
 */

public enum GameElementType {
	DISTRIBUTEUR(false, 15000),
	VERRE(true, 15000),
	CLAP(false, 0),
	PLOT(true, 0); // TODO: considérer les plots comme étant en commun? (vu que l'ennemi peut les shooter aussi...)

	private boolean canBeShot;
	private boolean isInCommon;
	private int dateEnemyTakesIt;
	
	private GameElementType(boolean canBeShot, int dateEnemyTakesIt)
	{
		this.canBeShot = canBeShot;
		this.dateEnemyTakesIt = dateEnemyTakesIt;
		this.isInCommon = (dateEnemyTakesIt != 0);
	}
	
	public boolean canBeShot()
	{
		return canBeShot;
	}
	
	public boolean isInCommon()
	{
		return isInCommon;
	}

	public long getDateEnemyTakesIt()
	{
		return dateEnemyTakesIt;
	}
	
}
