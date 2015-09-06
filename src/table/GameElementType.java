package table;

import scripts.ScriptHookNames;

/**
 * Type d'élément de jeux.
 * @author pf
 *
 */

// DEPENDS_ON_RULES

public enum GameElementType {
	DISTRIBUTEUR(15000, false, null, true, false),
	VERRE(15000, true, null, false, false),
	CLAP(0, false, null, true, true),
	PLOT(0, false, ScriptHookNames.EXEMPLE, true, false);

	private boolean isInCommon;
	private int dateEnemyTakesIt;
	private ScriptHookNames scriptHookThrown;
	public final boolean prenableEnMarcheAvant;
	public final boolean prenableEnMarcheArriere;
	public final boolean ejectable;
	
	private GameElementType(int dateEnemyTakesIt, boolean ejectable, ScriptHookNames scriptHookThrown, boolean prenableEnMarcheAvant, boolean prenableEnMarcheArriere)
	{
		this.ejectable = ejectable;
		this.prenableEnMarcheAvant = prenableEnMarcheAvant;
		this.prenableEnMarcheArriere = prenableEnMarcheArriere;
		this.dateEnemyTakesIt = dateEnemyTakesIt;
		this.isInCommon = (dateEnemyTakesIt != 0);
		this.scriptHookThrown = scriptHookThrown;
	}
	
	public boolean isPrenable(boolean enMarcheAvant)
	{
		return (!enMarcheAvant && prenableEnMarcheArriere) || (enMarcheAvant && prenableEnMarcheAvant);
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
