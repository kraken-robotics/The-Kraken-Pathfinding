package exceptions.strategie;

/**
 * Exception levée en cas de calcul de chemin impossible
 * @author pf
 *
 */

public class PathfindingException  extends Exception {

	private static final long serialVersionUID = -7968975910907981869L;

	public PathfindingException()
	{
		super();
	}
	
	public PathfindingException(String m)
	{
		super(m);
	}
	

}
