package exceptions;

/**
 * Exception levée lorsque le match est terminé dans l'AStar
 * @author pf
 *
 */

public class FinMatchFuturException extends Exception
{

	private static final long serialVersionUID = -960091158805232282L;

	public FinMatchFuturException()
	{
		super();
	}
	
	public FinMatchFuturException(String m)
	{
		super(m);
	}

}
