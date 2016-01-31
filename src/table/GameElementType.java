package table;

import scripts.ScriptHookNames;

/**
 * Type d'élément de jeux.
 * @author pf
 *
 */

public enum GameElementType {
	POISSONS(0, false, null, true, true),
	DRAPEAU(0, false, null, true, true),
	COQUILLAGE_NEUTRE(15000, true, null, true, true),
	COQUILLAGE_AMI(0, true, null, true, true),
	COQUILLAGE_ENNEMI(0, true, null, true, true),
	COQUILLAGE_EN_HAUTEUR_NEUTRE(0, false, null, true, true),
	COQUILLAGE_EN_HAUTEUR_AMI(0, false, null, true, true),
	COQUILLAGE_EN_HAUTEUR_ENNEMI(0, false, null, true, true),
	SABLE(5000, true, null, true, true);

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
