package hook.types;

import strategie.GameState;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;

/**
 * Un hook date reconnu facilement avec instanceof
 * @author pf
 *
 */

public class HookDateFinMatch extends HookDate
{
	
	private int dureeMatch = 90000;

	public HookDateFinMatch(Config config, Log log, GameState<?> state, long date)
	{
		super(config, log, state, date);
		dureeMatch = config.getInt(ConfigInfo.DUREE_MATCH_EN_S)*1000;
	}

	/**
	 * Mise à jour des callback de date 
	 * Très utilisé car cela permet d'arrêter la recherche plus tôt que prévu
	 * Sa date ne pourra jamais dépasser la fin du match.
	 * @param date_limite
	 */
	public void updateDate(int date_limite)
	{
		date_hook = Math.min(date_limite, dureeMatch);
	}

}
