package hook.types;

import exceptions.FinMatchException;
import hook.Hook;
import smartMath.Vec2;
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
     */
	public HookX(Config config, Log log, GameState<?> state, float xValue, float tolerancy)
	{
	    super(config, log, state);
		this.xValue = xValue;
		this.tolerancy = tolerancy;
	}

    /**
     * Déclenche le hook si la coordonnée x du robot est dans [xValue - tolerance, xValue + tolerance]
     * @return true si les déplacements du robot ont étés modifiés.
     */
	public boolean evaluate() throws FinMatchException
	{
		if(Math.abs(state.robot.getPosition().x-xValue) < tolerancy)
			return trigger();

		return false;
	}

	@Override
	public boolean simulated_evaluate(Vec2 pointA, Vec2 pointB, long date)
	{
		return (pointA.x - xValue) * (pointB.x - xValue) < 0;
	}
	
}
