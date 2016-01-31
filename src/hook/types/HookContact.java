package hook.types;

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
	
	public HookContact(Log log, int nbCapteur, boolean isUnique)
	{
		super(log, isUnique);
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
	public String toSerial()
	{
		String out = "Hct "+nbCapteur;
		if(isUnique)
			out += " T";
		else
			out += " F";
		out += " "+super.toSerial();
		return out;
	}
	
}
