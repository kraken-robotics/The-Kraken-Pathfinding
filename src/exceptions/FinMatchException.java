package exceptions;

/**
 * Exception levée lorsque le match est terminé.
 * @author pf
 *
 */

public class FinMatchException extends Exception
{

	private static final long serialVersionUID = -960091158805232282L;

	public FinMatchException()
	{
		super();
	}

	public FinMatchException(String m)
	{
		super(m);
	}

}
