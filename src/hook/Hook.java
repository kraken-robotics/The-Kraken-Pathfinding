package hook;

import java.util.ArrayList;

import permissions.ReadOnly;
import permissions.ReadWrite;
import exceptions.ChangeDirectionException;
import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import exceptions.WallCollisionDetectedException;
import robot.RobotChrono;
import strategie.GameState;
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

	protected ArrayList<Callback> callbacks = new ArrayList<Callback>();

	//gestion des log
	protected Log log;
	
	protected GameState<?,ReadOnly> state;

	protected static Integer rayon_robot = null;
	protected static Integer dureeMatch;
	
	/**
	 *  ce constructeur ne sera appellé que par les constructeurs des classes filles (des hooks bien précis)  
	 * @param config
	 * @param log
	 * @param real_state
	 */
	public Hook(Log log, GameState<?,ReadOnly> state)
	{
		this.log = log;
		this.state = state;
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
	public void ajouter_callback(Callback callback)
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
	public void trigger() throws FinMatchException, ScriptHookException, WallCollisionDetectedException, ChangeDirectionException
	{
		for(Callback callback : callbacks)
			callback.call();
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

	public void updateGameState(GameState<RobotChrono,ReadWrite> state)
	{
		this.state = state.getReadOnly();
		for(Callback callback : callbacks)
			callback.updateGameState(state);
	}
	
	/**
	 * Contient la commande à envoyer par série
	 * @return
	 */
	public ArrayList<String> toSerial()
	{
		ArrayList<String> out = new ArrayList<String>();
		out.add(String.valueOf(callbacks.size()));
		for(Callback c : callbacks)
			out.addAll(c.toSerial());
		return out;
	}

}

