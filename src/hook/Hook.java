package hook;

import java.util.ArrayList;

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
	
	//endroit ou lire la configuration du robot
	protected Config config;
	
	protected GameState<?> state;

	protected static Integer rayon_robot = null;
	
	/**
	 *  ce constructeur ne sera appellé que par les constructeurs des classes filles (des hooks bien précis)  
	 * @param config
	 * @param log
	 * @param real_state
	 */
	public Hook(Config config, Log log, GameState<?> state)
	{
		this.config = config;
		this.log = log;
		this.state = state;
		
		// Cette optimisation est nécessaire car une lecture
		// en config est très lente et on crée beaucoup de hook.
		if(rayon_robot == null)
			rayon_robot = config.getInt(ConfigInfo.RAYON_ROBOT);
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
	public abstract void evaluate() throws FinMatchException, ScriptHookException, WallCollisionDetectedException, ChangeDirectionException;
	
	/**
	 * Méthode appelée par RobotChrono. Elle doit dire si, sur un trajet entre A et B et
	 * à une certaine date, le hook est sensé s'activer.
	 * @param pointA
	 * @param pointB
	 * @param date
	 * @return
	 */
	public abstract boolean simulated_evaluate(Vec2 pointA, Vec2 pointB, long date);
	
	/**
	 * On peut supprimer le hook s'il n'y a plus aucun callback déclenchable.
	 * @return
	 */
	public boolean supprimable()
	{
	    for(Callback c: callbacks)
	        if(!c.shouldBeDeleted())
	            return false;
	    return true;
	}

	public void updateGameState(GameState<RobotChrono> state)
	{
		this.state = state;
		for(Callback callback : callbacks)
			callback.updateGameState(state);
	}

}

