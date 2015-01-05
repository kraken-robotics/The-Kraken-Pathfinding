package exceptions;

/**
 * Exception levée par la config
 * @author pf
 *
 */

public class ConfigException extends Exception
{

	private static final long serialVersionUID = -960091158805232282L;

	public ConfigException()
	{
		super();
	}
	
	public ConfigException(String m)
	{
		super(m);
	}

}
