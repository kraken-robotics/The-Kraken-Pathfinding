package exceptions;

/**
 * Exception levée par SerialConnexion
 * @author pf
 *
 */

public class MissingCharacterException extends Exception
{

	private static final long serialVersionUID = -960091158805232282L;

	public MissingCharacterException()
	{
		super();
	}
	
	public MissingCharacterException(String m)
	{
		super(m);
	}

}
