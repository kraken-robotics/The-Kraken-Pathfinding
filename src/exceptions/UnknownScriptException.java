package exceptions;

/**
 * Exception levée en cas de demande d'un script inconnu
 * @author marsu
 *
 */

public class UnknownScriptException  extends Exception
{
	private static final long serialVersionUID = -3039558414266587469L;

	public UnknownScriptException()
	{
		super();
	}
	
	public UnknownScriptException(String m)
	{
		super(m);
	}
	

}
