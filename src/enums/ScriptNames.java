package enums;

/**
 * Enumération des noms des scripts
 * 
 * @author pf
 *
 */

public enum ScriptNames {
	SortieZoneDepart(false), // false = pas pris en compte dans la stratégie
	ScriptClap(true),
	ScriptTapis(true),
	ScriptAttente(true);
	
	private boolean canIDoIt; // ce booléan dépend du robot!
	// si on a deux robots, ils ne pourront pas faire la même chose...
	
	ScriptNames(boolean canIDoIt)
	{
		this.canIDoIt = canIDoIt;
	}
	
	public boolean canIDoIt()
	{
		return canIDoIt;
	}
	
}
