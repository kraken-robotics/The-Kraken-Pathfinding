package hook.types;

import java.util.ArrayList;

import pathfinding.GameState;
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
	public HookDate(Log log, GameState<?,ReadOnly> state, long date)
	{
		super(log, state);
		this.date_hook = date;
	}
	
	@Override
	public boolean simulated_evaluate(Vec2<ReadOnly> pointA, Vec2<ReadOnly> pointB, long date)
	{
//		log.debug("Hook date: appel="+date_appel+", date_hook="+this.date_hook, this);
		return date > this.date_hook;
	}

	@Override
	public ArrayList<String> toSerial()
	{
		ArrayList<String> out = new ArrayList<String>();
		out.add("hda");
		out.add(String.valueOf(date_hook));
		out.addAll(super.toSerial());
		return out;
	}
	
}
