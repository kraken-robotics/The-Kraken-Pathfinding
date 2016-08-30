package exceptions.serie;

/**
 * Exception levée par la série en cas d'erreur de protocole
 * @author pf
 *
 */

public class ProtocolException extends Exception
{

	private static final long serialVersionUID = -960091158805232282L;

	public ProtocolException()
	{
		super();
	}
	
	public ProtocolException(String m)
	{
		super(m);
	}

}
