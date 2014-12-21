package enums;

/**
 * Type d'élément de jeux.
 * @author pf
 *
 */

public enum GameElementType {
	DISTRIBUTEUR(false, true),
	VERRE(true, true),
	CLAP(false, false),
	PLOT(true, false); // TODO: considérer les plots comme étant en commun? (vu que l'ennemi peut les shooter aussi...)

	private boolean canBeShot;
	private boolean isInCommon;
	
	private GameElementType(boolean canBeShot, boolean isInCommon)
	{
		this.canBeShot = canBeShot;
		this.isInCommon = isInCommon;
	}
	
	public boolean canBeShot()
	{
		return canBeShot;
	}
	
	public boolean isInCommon()
	{
		return isInCommon;
	}
	
}
