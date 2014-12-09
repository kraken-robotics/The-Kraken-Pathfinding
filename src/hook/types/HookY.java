package hook.types;

import exceptions.FinMatchException;
import hook.Hook;
import smartMath.Vec2;
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
    public HookY(Config config, Log log, GameState<?> state, float yValue, float tolerancy)
    {
        super(config, log, state);
        this.yValue = yValue;
        this.tolerancy = tolerancy;
    }
    
    /**
     * Déclenche le hook si la coordonnée y du robot est dans [yValue - tolerance, yValue + tolerance]
     * @return true si les déplacements du robot ont étés modifiés par cette méthode.
     */
    public boolean evaluate() throws FinMatchException
    {
        if(Math.abs(state.robot.getPosition().y-yValue) < tolerancy)
            return trigger();

        return false;
    }
    
	@Override
	public boolean simulated_evaluate(Vec2 pointA, Vec2 pointB, long date)
	{
		return (pointA.y - yValue) * (pointB.y - yValue) < 0;
	}

}
