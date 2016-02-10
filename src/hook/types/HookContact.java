package hook.types;

import permissions.ReadOnly;
import utils.Log;
import utils.Vec2;

import java.util.ArrayList;

import enums.SerialProtocol;
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
	public ArrayList<Byte> toSerial()
	{
		ArrayList<Byte> out = new ArrayList<Byte>();
		if(isUnique)
			out.add(SerialProtocol.HOOK_CONTACT_UNIQUE.nb);
		else
			out.add(SerialProtocol.HOOK_CONTACT.nb);
		out.add((byte) nbCapteur);
		out.addAll(super.toSerial());
		return out;
	}
	
}
