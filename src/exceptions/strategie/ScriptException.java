package exceptions.strategie;

/**
 * Exception levée par un script
 * @author pf
 *
 */
public class ScriptException extends Exception {

	private static final long serialVersionUID = 4219866343407002284L;

	public ScriptException()
	{
		super();
	}
	
	public ScriptException(String m)
	{
		super(m);
	}

}
