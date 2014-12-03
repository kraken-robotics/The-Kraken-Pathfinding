package exceptions;

/**
 * Exception levée par le pathfinding
 * @author pf
 *
 */

public class PathfindingRobotInObstacleException extends Exception
{

	private static final long serialVersionUID = -960091158805232282L;

	public PathfindingRobotInObstacleException()
	{
		super();
	}
	
	public PathfindingRobotInObstacleException(String m)
	{
		super(m);
	}

}
