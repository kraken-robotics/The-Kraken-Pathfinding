package hook;

import java.util.ArrayList;

import permissions.ReadOnly;
import exceptions.FinMatchException;
import utils.ConfigInfo;
import utils.Log;
import utils.Config;
import utils.Vec2;

/**
 * Classe-mère abstraite des hooks, utilisés pour la programmation évènementielle
 * @author pf
 *
 */

abstract public class Hook
{

	protected ArrayList<Executable> callbacks = new ArrayList<Executable>();

	protected Log log;
	
	protected static int rayon_robot;
	protected static int dureeMatch;
	private static int numStatic = 0;
	private int num;
	protected boolean isDone = false;
	protected boolean isUnique;
	
	/**
	 * Ce constructeur ne sera appelé que par les constructeurs des classes filles
	 * @param config
	 * @param log
	 * @param real_state
	 */
	public Hook(Log log, boolean isUnique)
	{
		num = numStatic;
		numStatic++;
		this.isUnique = isUnique;
		this.log = log;
	}
	
	public static void useConfig(Config config)
	{
		rayon_robot = config.getInt(ConfigInfo.RAYON_ROBOT);
		dureeMatch = config.getInt(ConfigInfo.DUREE_MATCH_EN_S)*1000;
	}
	
	/**
	 * On peut ajouter un callback à un hook.
	 * Il n'y a pas de méthode pour en retirer, car il n'y en a a priori pas besoin
	 * @param callback
	 */
	public void ajouter_callback(Executable callback)
	{
		callbacks.add(callback);
	}
	
	/**
	 * Déclenche le hook.
	 * Tous ses callbacks sont exécutés
	 * @throws FinMatchException 
	 * @throws ScriptHookException 
	 * @throws WallCollisionDetectedException 
	 * @throws ChangeDirectionException 
	 */
	public void trigger()
	{
		if(!isDone || !isUnique)
			for(Executable callback : callbacks)
				callback.execute();
	}

	/**
	 * Méthode qui sera surchargée par les classes filles.
	 * Elle contient la condition d'appel du hook
	 * Elle est appelée par RobotVrai.
	 * @param robot
	 * @throws ScriptHookException 
	 * @throws WallCollisionDetectedException 
	 * @throws ChangeDirectionException 
	 */
	// Cette méthode a été transférée au bas niveau
//	public abstract void evaluate() throws FinMatchException, ScriptHookException, WallCollisionDetectedException, ChangeDirectionException;
	
	/**
	 * Méthode appelée par RobotChrono. Elle doit dire si, sur un trajet entre A et B et
	 * à une certaine date, le hook est sensé s'activer.
	 * @param pointA
	 * @param pointB
	 * @param date
	 * @return
	 */
	public abstract boolean simulated_evaluate(Vec2<ReadOnly> pointA, Vec2<ReadOnly> pointB, long date);
	
	/**
	 * On peut supprimer le hook s'il n'y a plus aucun callback déclenchable.
	 * @return
	 */
 /*	public boolean supprimable()
	{
	    for(Callback c: callbacks)
	        if(!c.shouldBeDeleted())
	            return false;
	    return true;
	}*/

/*	public void updateGameState(GameState<RobotChrono,ReadWrite> state)
	{
		this.state = state.getReadOnly();
		for(Executable callback : callbacks)
			callback.updateGameState(state);
	}
	*/
	
	/**
	 * Contient la commande à envoyer par série
	 * @return
	 */
	public ArrayList<Byte> toSerial()
	{
		ArrayList<Byte> out = new ArrayList<Byte>();
		out.add((byte)num);
		out.add((byte)callbacks.size());
		for(Executable c : callbacks)
			out.addAll(c.toSerial());
		return out;
	}
	
	/**
	 * Renvoie le numéro du hook
	 * @return
	 */
	public int getNum()
	{
		return num;
	}

}

