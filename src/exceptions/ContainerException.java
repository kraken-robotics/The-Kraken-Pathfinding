package exceptions;

/**
 * Exception levée par le container
 * @author pf
 *
 */

public class ContainerException extends Exception
{

	private static final long serialVersionUID = -960091158805232282L;

	public ContainerException()
	{
		super();
	}
	
	public ContainerException(String m)
	{
		super(m);
	}

}
