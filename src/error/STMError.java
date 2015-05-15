package error;

/**
 * Une classe qui contient les informations en cas d'erreur soulevée par le bas niveau
 * @author pf
 *
 */

public class STMError {

	public Error erreur;
	
	private static final STMError instance = new STMError();
	
	private STMError()
	{}
	
	public static STMError getInstance()
	{
		return instance;
	}
	
}
