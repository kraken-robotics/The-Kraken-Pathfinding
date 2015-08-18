package hook.types;

import java.util.ArrayList;

import hook.Hook;
import exceptions.FinMatchException;
import obstacles.ObstacleCircular;
import obstacles.ObstacleRectangular;
import permissions.ReadOnly;
import permissions.ReadWrite;
import strategie.GameState;
import utils.Log;
import utils.Vec2;

/**
 * Hook pour gérer la collision avec les éléments de jeux
 * @author pf
 *
 */

public class HookCollisionElementJeu extends Hook
{
	private ObstacleCircular obstacle;
	private ObstacleRectangular obstacleRobot;
	
	public HookCollisionElementJeu(Log log, GameState<?, ReadOnly> state, ObstacleCircular o) throws FinMatchException
	{
		super(log, state);
		obstacle = o;
		obstacleRobot = new ObstacleRectangular(state);
	}

	@Override
	public boolean simulated_evaluate(Vec2<ReadOnly> pointA, Vec2<ReadOnly> pointB, long date)
	{
		try {
			// TODO
			ObstacleRectangular.update(obstacleRobot, GameState.getPosition(state), GameState.getOrientation(state));
		} catch (FinMatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ObstacleRectangular r = new ObstacleRectangular(pointA, pointB);
		return r.isColliding(obstacle);
	}
	
    /**
     * Déclenche le hook si la distance entre la position du robot et la position de de déclenchement du hook est inférieure a tolerancy
     * @return true si la position/oriantation du robot a été modifiée.
     * @throws ScriptHookException 
     * @throws WallCollisionDetectedException 
     * @throws ChangeDirectionException 
     */
/*	@Override
	public void evaluate() throws FinMatchException, ScriptHookException, WallCollisionDetectedException, ChangeDirectionException
	{
		ObstacleRectangular.update(obstacleRobot, GameState.getPosition(state), GameState.getOrientation(state));
		if(obstacleRobot.isColliding(obstacle))
			trigger();
	}*/

	@Override
	public ArrayList<String> toSerial()
	{
		ArrayList<String> out = new ArrayList<String>();
		out.add("co");
		// TODO: ajouter numéro de l'élément de jeu
		return out;
	}	
}
