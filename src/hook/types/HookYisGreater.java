package hook.types;

import hook.types.HookY;
import robot.RobotReal;
import strategie.GameState;
import utils.Log;
import utils.Config;


/**
 * Déclenchement sur la coordonnée Y du robot sur la table
 * le robot doit être au dessus d'une droite Y = constante
 * @author pf, marsu
 */


class HookYisGreater extends HookY
{
    /**
     * Instancie le hook sur coordonnée Y. Valeur en Y et tolérance paramétrable.
     * @param config : sur quel objet lire la configuration du match
     * @param log : la sortie de log à utiliser
     * @param realState : lien avec le robot a surveiller pour le déclenchement du hook
     * @param yValue : la valeur en y ou doit se déclencher le hook
     */
    public HookYisGreater(Config config, Log log, GameState<RobotReal> realState, float yValue)
    {
        super(config, log, realState, yValue, 0);
    }
    

    /**
     * Déclenche le hook si la coordonnée y du robot est plus grande ou égale que yValue
     * @return true si les déplacements du robot ont étés modifiés par cette méthode.
     */
    @Override
    public boolean evaluate()
    {
    	if(real_state.robot.getPosition().y >= yValue)
            return trigger();

        return false;
    }
    
}
