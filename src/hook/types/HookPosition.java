package hook.types;

import hook.Hook;
import robot.RobotReal;
import smartMath.Vec2;
import strategie.GameState;
import utils.Log;
import utils.Config;

/**
 * Hook se déclenchant si le robot est a une distance a un point de la table inférieure a un certain seuil
 * La zone d'activation est un disque.
 * @author pf, marsu
 *
 */

class HookPosition extends Hook
{
	// position sur la table de déclenchement du hook: le hook est déclenché si le robot est a une distance de ce point de moins de tolerancy
	private Vec2 position;
	
	// tolérance sur la position de déclenchement du hook. On mémorise le carré pour ne pas avoir a calculer des racines a chaque vérifications
	private int squaredTolerancy;
	
	

    /**
     * Instancie le hook sur position du robot. Position et tolérance paramétrable.
     * @param config : sur quel objet lire la configuration du match
     * @param log : la sortie de log à utiliser
     * @param realState : lien avec le robot a surveiller pour le déclenchement du hook
     * @param position : la valeur en y ou doit se déclencher le hook
     * @param tolerance : imprécision admise sur la position qui déclenche le hook
     * @param isYellowTeam : la couleur du robot: vert ou jaune 
     */
	public HookPosition(Config config, Log log, GameState<RobotReal> realState, Vec2 position, int tolerancy, boolean isYellowTeam)
	{
		super(config, log, realState);
		this.position = position;
		this.squaredTolerancy = tolerancy*tolerancy;
		if(isYellowTeam)
			position.x *= -1;
	}
	

    /**
     * Déclenche le hook si la distance entre la position du robot et la position de de déclenchement du hook est inférieure a tolerancy
     * @return true si la position/oriantation du robot a été modifiée.
     */
	public boolean evaluate()
	{
		Vec2 positionRobot = real_state.robot.getPosition();
		if(position.squaredDistance(positionRobot) <= squaredTolerancy)
			return trigger();
		return false;
	}
	
}
