package pathfinding;

import robot.Robot;
import table.Table;

/**
 * GameState abstrait. Permet juste d'avoir le robot et la table pour les scripts.
 * @author pf
 *
 */

public abstract class GameState<R extends Robot>
{
	public Table table;
	public R robot;
	public abstract void copyAStarCourbe(ChronoGameState state);
}
