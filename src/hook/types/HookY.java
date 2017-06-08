package hook.types;

import hook.Hook;
import robot.RobotReal;
import strategie.GameState;
import utils.Log;
import utils.Config;

/**
 * Déclenchement sur la coordonnée Y du robot sur la table
 * le robot doit être proche d'une droite Y = constante
 * @author pf, marsu
 */

class HookY extends Hook
{
	// l'endroit ou est déclenché le hook
    protected float yValue;
    
    // imprécision sur la position de déclenchement du hook. (en milimètres aussi)
    // Le hook sera déclenché si la coordonnée y du robot est dans [yValue - tolerance, yValue + tolerance]
    private float tolerancy;
    
    /**
     * Instancie le hook sur coordonnée Y. Valeur en Y et tolérance paramétrable.
     * @param config : sur quel objet lire la configuration du match
     * @param log : la sortie de log à utiliser
     * @param realState : lien avec le robot a surveiller pour le déclenchement du hook
     * @param yValue : la valeur en y ou doit se déclencher le hook
     * @param tolerancy : imprécision admise sur la position qui déclenche le hook
     */
    public HookY(Config config, Log log, GameState<RobotReal> realState, float yValue, float tolerancy)
    {
        super(config, log, realState);
        this.yValue = yValue;
        this.tolerancy = tolerancy;
    }
    
    /**
     * Déclenche le hook si la coordonnée y du robot est dans [yValue - tolerance, yValue + tolerance]
     * @return true si les déplacements du robot ont étés modifiés par cette méthode.
     */
    public boolean evaluate()
    {
        if(Math.abs(real_state.robot.getPosition().y-yValue) < tolerancy)
            return trigger();

        return false;
    }
    
}
