package hook.types;

import java.util.ArrayList;

import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import exceptions.WallCollisionDetectedException;
import strategie.GameState;
import utils.Config;
import utils.Log;
import utils.Vec2;
import hook.Hook;

public class HookDemiPlan extends Hook
{

	private Vec2 point, direction;
	private ArrayList<Vec2> itineraire;
	private boolean disabled = false;
	
	/**
	 * point appartient à la ligne qui sépare le demi-plan
	 * direction est un vecteur normal à la ligne de séparation de deux demi-plans, et
	 * qui pointe en direction du plan qui active le hook
	 * @param config
	 * @param log
	 * @param state
	 * @param point
	 * @param direction
	 */
	public HookDemiPlan(Config config, Log log, GameState<?> state, Vec2 point, Vec2 direction)
	{		
		super(config, log, state);
		this.point = point;
		this.direction = direction;
	}

	/**
	 * On suppose que itineraire.size() >= 3
	 * @param config
	 * @param log
	 * @param state
	 * @param itineraire
	 */
	public HookDemiPlan(Config config, Log log, GameState<?> state, ArrayList<Vec2> itineraire)
	{		
		super(config, log, state);
		this.itineraire = itineraire;
		update();
	}

	
	@Override
	public void evaluate() throws FinMatchException, ScriptHookException,
			WallCollisionDetectedException
	{
		Vec2 positionRobot = state.robot.getPosition();
		if(!disabled && positionRobot.minusNewVector(point).dot(direction) > 0)
		{
			trigger();
			update();
		}
		
	}
	
	/**
	 * Mise à jour de la condition dans le cas d'un itinéraire de trajectoire courbe
	 */
	private void update()
	{
		if(itineraire != null)
		{
			if(itineraire.size() >= 3)
			{
				Vec2 A = itineraire.get(0);
				Vec2 B = itineraire.get(1);
				double normeAB = A.distance(B);
				double longueur_anticipation = 200; // TODO: à calculer en fonction de A et B
				Vec2 C = B.plusNewVector(A.minusNewVector(B).scalarNewVector(Math.min(longueur_anticipation, normeAB)/normeAB));
				this.point = C;
				this.direction = B.minusNewVector(C);
				itineraire.remove(0);
			}
			else
				disabled = true;
		}
	}

	/**
	 * Pour vérifier si le hook peut être déclenché sur un segment [A,B], il
	 * faut juste vérifier s'il peut l'être en A ou en B.
	 */
	@Override
	public boolean simulated_evaluate(Vec2 pointA, Vec2 pointB, long date) {
		return pointA.minusNewVector(point).dot(direction) > 0 ||
				pointB.minusNewVector(point).dot(direction) > 0;
	}

}
