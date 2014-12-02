package exceptions;

/**
 * Exception levée par le pathfinding
 * @author pf
 *
 */

public class PathfindingException extends Exception
{

	private static final long serialVersionUID = -960091158805232282L;

	public PathfindingException()
	{
		super();
	}
	
	public PathfindingException(String m)
	{
		super(m);
	}

}
