package exceptions.serial;

/**
 * Exception levée par les connexions séries
 * @author pf
 *
 */
public class SerialConnexionException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1826278884421114631L;

	public SerialConnexionException()
	{
		super();
	}
	
	public SerialConnexionException(String m)
	{
		super(m);
	}
}
