package hook.types;

import obstacles.ObstacleCircular;
import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import hook.Hook;
import strategie.GameState;
import utils.Log;
import utils.Config;
import utils.Vec2;

/**
 * Hook se déclenchant si le robot est a une distance a un point de la table inférieure a un certain seuil
 * La zone d'activation est un disque.
 * @author pf, marsu
 *
 */

public class HookPosition extends Hook
{
	// position sur la table de déclenchement du hook: le hook est déclenché si le robot est a une distance de ce point de moins de tolerancy
	private Vec2 position;
	
	// tolérance sur la position de déclenchement du hook. On mémorise le carré pour ne pas avoir a calculer des racines a chaque vérifications
	private int squaredTolerancy;
	
	private int tolerancy;
	

    /**
     * Instancie le hook sur position du robot. Position et tolérance paramétrable.
     * @param config : sur quel objet lire la configuration du match
     * @param log : la sortie de log à utiliser
     * @param realState : lien avec le robot a surveiller pour le déclenchement du hook
     * @param position : la valeur en y ou doit se déclencher le hook
     * @param tolerance : imprécision admise sur la position qui déclenche le hook
     * @param isYellowTeam : la couleur du robot: vert ou jaune 
     */
	public HookPosition(Config config, Log log, GameState<?> state, Vec2 position, int tolerancy)
	{
		super(config, log, state);
		this.position = position;
		this.tolerancy = tolerancy;
		this.squaredTolerancy = tolerancy*tolerancy;
	}
	

    /**
     * Déclenche le hook si la distance entre la position du robot et la position de de déclenchement du hook est inférieure a tolerancy
     * @return true si la position/oriantation du robot a été modifiée.
     * @throws ScriptHookException 
     */
	public void evaluate() throws FinMatchException, ScriptHookException
	{
		Vec2 positionRobot = state.robot.getPosition();
		if(position.squaredDistance(positionRobot) <= squaredTolerancy)
			trigger();
	}


	@Override
	public boolean simulated_evaluate(Vec2 pointA, Vec2 pointB, long date)
	{
		ObstacleCircular o = new ObstacleCircular(position, tolerancy);
//		log.debug("Hook position: "+o.obstacle_proximite_dans_segment(pointA, pointB, rayon_robot), this);
		return o.obstacle_proximite_dans_segment(pointA, pointB, rayon_robot);
	}
	
}
