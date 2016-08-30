package exceptions.serie;

/**
 * Exception levée par la série
 * @author pf
 *
 */

public class IncorrectChecksumException extends Exception
{

	private static final long serialVersionUID = -960091158805232282L;

	public IncorrectChecksumException()
	{
		super();
	}
	
	public IncorrectChecksumException(String m)
	{
		super(m);
	}

}
