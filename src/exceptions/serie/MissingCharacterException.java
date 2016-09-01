package exceptions.serie;

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
		super("Un caractère attendu n'est pas arrivé");
	}
}
