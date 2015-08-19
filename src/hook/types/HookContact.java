package hook.types;

import java.util.ArrayList;

import permissions.ReadOnly;
import strategie.GameState;
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
	
	public HookContact(Log log, GameState<?, ReadOnly> state, int nbCapteur)
	{
		super(log, state);
		this.nbCapteur = nbCapteur;
	}

/*	@Override
	public void evaluate() throws FinMatchException, ScriptHookException, WallCollisionDetectedException, ChangeDirectionException {
		if(System.currentTimeMillis() - Config.getDateDebutMatch() > date_hook)
			trigger();
	}*/

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
		out.add("hda");
		out.add(String.valueOf(nbCapteur));
		out.addAll(super.toSerial());
		return out;
	}
	
}
