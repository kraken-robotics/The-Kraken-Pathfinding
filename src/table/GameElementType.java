package table;

import scripts.ScriptHookNames;

/**
 * Type d'élément de jeux.
 * @author pf
 *
 */

public enum GameElementType {
	DISTRIBUTEUR(false, 15000, null),
	VERRE(true, 15000, ScriptHookNames.PREND_VERRE),
	CLAP(false, 0, null),
	PLOT(true, 0, ScriptHookNames.PREND_PLOT); // TODO: considérer les plots comme étant en commun? (vu que l'ennemi peut les shooter aussi...)

	private boolean canBeShot;
	private boolean isInCommon;
	private int dateEnemyTakesIt;
	private ScriptHookNames scriptHookThrown;
	
	private GameElementType(boolean canBeShot, int dateEnemyTakesIt, ScriptHookNames scriptHookThrown)
	{
		this.canBeShot = canBeShot;
		this.dateEnemyTakesIt = dateEnemyTakesIt;
		this.isInCommon = (dateEnemyTakesIt != 0);
		this.scriptHookThrown = scriptHookThrown;
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
	
	public ScriptHookNames scriptHookThrown()
	{
		return scriptHookThrown;
	}
	
}
