package hook.types;

import hook.Hook;
import robot.RobotReal;
import strategie.GameState;
import utils.Log;
import utils.Config;

/**
 * déclenchement sur la coordonnée X du robot sur la table
 * @author pf, marsu
 */

class HookX extends Hook
{
	// l'endroit ou est déclenché le hook
	protected float xValue;
	
	// imprécision sur la position de déclenchement du hook. (en milimètres aussi)
    // Le hook sera déclenché si la coordonnée x du robot est dans [xValue - tolerance, xValue + tolerance]
    private float tolerancy;

    /**
     * Instancie le hook sur coordonnée Y. Valeur en Y et tolérance paramétrable.
     * @param config : sur quel objet lire la configuration du match
     * @param log : la sortie de log à utiliser
     * @param realState : lien avec le robot a surveiller pour le déclenchement du hook
     * @param ordonnee : la valeur en y ou doit se déclencher le hook
     * @param tolerancy : imprécision admise sur la position qui déclenche le hook
     * @param isYellowTeam : la couleur du robot: vert ou jaune 
     */
	public HookX(Config config, Log log, GameState<RobotReal> realState, float xValue, float tolerancy, boolean isYellowTeam)
	{
	    super(config, log, realState);
		this.xValue = xValue;
		this.tolerancy = tolerancy;
		if(isYellowTeam)
			xValue *= -1;
	}

    /**
     * Déclenche le hook si la coordonnée x du robot est dans [xValue - tolerance, xValue + tolerance]
     * @return true si les déplacements du robot ont étés modifiés.
     */
	public boolean evaluate()
	{
		if(Math.abs(real_state.robot.getPosition().x-xValue) < tolerancy)
			return trigger();

		return false;
	}
	
}
