package hook.types;

import exceptions.FinMatchException;
import hook.types.HookY;
import smartMath.Vec2;
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
    public HookYisGreater(Config config, Log log, GameState<?> state, float yValue)
    {
        super(config, log, state, yValue, 0);
    }
    

    /**
     * Déclenche le hook si la coordonnée y du robot est plus grande ou égale que yValue
     * @return true si les déplacements du robot ont étés modifiés par cette méthode.
     */
    @Override
    public boolean evaluate() throws FinMatchException
    {
    	if(state.robot.getPosition().y >= yValue)
            return trigger();

        return false;
    }
    
	@Override
	public boolean simulated_evaluate(Vec2 pointA, Vec2 pointB, long date)
	{
		return (pointA.y > yValue) || (pointB.y > yValue);
	}

}
