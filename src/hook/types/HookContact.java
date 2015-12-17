package hook.types;

import java.util.ArrayList;

import pathfinding.GameState;
import permissions.ReadOnly;
import utils.Log;
import utils.Vec2;
import hook.Hook;

/**
 * Hook déclenché par un capteur de contact
 * @author pf
 *
 */

public class HookContact extends Hook {

	private int nbCapteur;
	
	public HookContact(Log log, GameState<?, ReadOnly> state, int nbCapteur, boolean isUnique)
	{
		super(log, state, isUnique);
		this.nbCapteur = nbCapteur;
	}

	/**
	 * Ce hook n'est pas simulable
	 */
	@Override
	public boolean simulated_evaluate(Vec2<ReadOnly> pointA, Vec2<ReadOnly> pointB, long date)
	{
		return false;
	}

	@Override
	public ArrayList<String> toSerial()
	{
		ArrayList<String> out = new ArrayList<String>();
		out.add("Hct");
		out.add(String.valueOf(nbCapteur));
		if(isUnique)
			out.add("T");
		else
			out.add("F");
		out.addAll(super.toSerial());
		return out;
	}
	
}
