package exceptions.strategie;

/**
 * Exception levée par les scripts
 * @author pf
 *
 */
public class ScriptException extends Exception
{
	private static final long serialVersionUID = 1826278884421114631L;

	public ScriptException()
	{
		super();
	}
	
	public ScriptException(String m)
	{
		super(m);
	}
}
