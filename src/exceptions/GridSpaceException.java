package exceptions;

/**
 * Exception levée par le gridspace
 * @author pf
 *
 */

public class GridSpaceException extends Exception
{

	private static final long serialVersionUID = -960091158805232282L;

	public GridSpaceException()
	{
		super();
	}
	
	public GridSpaceException(String m)
	{
		super(m);
	}

}
