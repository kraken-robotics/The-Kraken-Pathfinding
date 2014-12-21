package hook.types;

import strategie.GameState;
import utils.Config;
import utils.Log;

/**
 * Un hook date reconnu facilement avec instanceof
 * @author pf
 *
 */

public class HookDateFinMatch extends HookDate
{

	public HookDateFinMatch(Config config, Log log, GameState<?> state, long date)
	{
		super(config, log, state, date);
	}

	/**
	 * Mise à jour des callback de date 
	 * Très utilisé car cela permet d'arrêter la recherche plus tôt que prévu
	 * @param date_limite
	 */
	public void updateDate(int date_limite)
	{
		date_hook = date_limite;
	}

}
