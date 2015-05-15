package requete;

/**
 * Une classe qui contient les informations en cas d'erreur soulevée par le bas niveau
 * @author pf
 *
 */

public class RequeteSTM {

	public RequeteType type;
	
	private static final RequeteSTM instance = new RequeteSTM();
	
	private RequeteSTM()
	{}
	
	public static RequeteSTM getInstance()
	{
		return instance;
	}
	
}
