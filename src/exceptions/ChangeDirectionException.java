package exceptions;

/**
 * Exception utilisée pour les trajectoires courbes
 * @author pf
 *
 */

public class ChangeDirectionException extends Exception
{

	private static final long serialVersionUID = -960091158805232282L;

	public ChangeDirectionException()
	{
		super();
	}
	
	public ChangeDirectionException(String m)
	{
		super(m);
	}

}
