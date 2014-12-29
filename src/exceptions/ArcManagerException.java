package exceptions;

/**
 * Exception levée par un arc manager
 * @author pf
 *
 */

public class ArcManagerException extends Exception
{

	private static final long serialVersionUID = -960091158805232282L;

	public ArcManagerException()
	{
		super();
	}
	
	public ArcManagerException(String m)
	{
		super(m);
	}

}
