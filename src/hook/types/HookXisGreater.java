package hook.types;

import hook.types.HookX;
import robot.RobotReal;
import strategie.GameState;
import utils.Log;
import utils.Config;

/**
 * déclenche un évènement dès que le robot a une coordonnée X (sur la table) supérieure a celle fournie
 * @author pf, marsu
 *
 */

class HookXisGreater extends HookX
{

    /**
     * Instancie le hook sur coordonnée X. Valeur en X et tolérance paramétrable.
     * @param config : sur quel objet lire la configuration du match
     * @param log : la sortie de log à utiliser
     * @param realState : lien avec le robot a surveiller pour le déclenchement du hook
     * @param xValue : la valeur en x ou doit se déclencher le hook
     * @param tolerancy : imprécision admise sur la position qui déclenche le hook
	 * @param isYellowTeam La table étant symétrisée si l'on est équipe jaune, le XisGreater devient un XisLesser si l'on est jaune
     */
    public HookXisGreater(Config config, Log log,GameState<RobotReal> realState, float xValue, float tolerancy, boolean isYellowTeam)
    {
		super(config, log, realState, xValue, tolerancy, isYellowTeam);
	}
    

    /**
     * Déclenche le hook si la coordonnée x du robot est plus grande que xValue
     * @return true si la position/orientation du robot a été modifiée par cette méthode.
     */
    @Override
    public boolean evaluate()
    {
        if(real_state.robot.getPosition().x > xValue)
            return trigger();

        return false;
    }
    
}
