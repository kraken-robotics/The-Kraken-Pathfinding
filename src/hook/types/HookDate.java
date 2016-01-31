package hook.types;

import permissions.ReadOnly;
import utils.Log;
import utils.Vec2;
import hook.Hook;

/**
 * Hook déclenché sur une date
 * @author pf
 *
 */

public class HookDate extends Hook {

	protected long date_hook;
	
	/**
	 * La date est le nombre de ms depuis le début du match
	 * @param log
	 * @param state
	 * @param date
	 */
	public HookDate(Log log, long date)
	{
		super(log, true);
		this.date_hook = date;
	}
	
	@Override
	public boolean simulated_evaluate(Vec2<ReadOnly> pointA, Vec2<ReadOnly> pointB, long date)
	{
//		log.debug("Hook date: appel="+date_appel+", date_hook="+this.date_hook, this);
		return date > this.date_hook;
	}

	@Override
	public String toSerial()
	{
		return "Hda "+date_hook+" "+super.toSerial();
	}
	
}
