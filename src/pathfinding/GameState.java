package pathfinding;

import robot.Robot;
import table.Table;

/**
 * GameState abstrait. Permet juste d'avoir le robot.
 * @author pf
 *
 */

public abstract class GameState
{
	public Table table;
	public abstract Robot getRobot();	
	public abstract void copyAStarCourbe(ChronoGameState state);
}
