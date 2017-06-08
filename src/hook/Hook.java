package hook;

import java.util.ArrayList;

import robot.RobotReal;
import strategie.GameState;
import utils.Log;
import utils.Config;

/**
 * Classe-mère abstraite des hooks, utilisés pour la programmation évènementielle
 * @author pf
 *
 */

abstract public class Hook
{

	protected ArrayList<Callback> callbacks = new ArrayList<Callback>();

	//gestion des log
	@SuppressWarnings("unused")
	private Log log;
	
	//endroit ou lire la configuration du robot
	@SuppressWarnings("unused")
	private Config config;
	
	protected GameState<RobotReal> real_state;

	/**
	 *  ce constructeur ne sera appellé que par les constructeurs des classes filles (des hooks bien précis)  
	 * @param config
	 * @param log
	 * @param real_state
	 */
	public Hook(Config config, Log log, GameState<RobotReal> real_state)
	{
		this.config = config;
		this.log = log;
		this.real_state = real_state;
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
	 * @return true si ce hook modifie les déplacements du robot
	 */
	protected boolean trigger()
	{
		boolean retour = false;
		
		for(Callback callback : callbacks)
			retour |= callback.call();
		return retour;
	}

	/**
	 * Méthode qui sera surchargée par les classes filles.
	 * Elle contient la condition d'appel du hook
	 * @param robot
	 * @return true si ce hook modifie les déplacements du robot, false sinon
	 */
	public abstract boolean evaluate();
	
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

}

