package exceptions;

/**
 * Exception levée lorsque les points de sortie ne sont pas corrects
 * @author pf
 *
 */

public class PointSortieException extends Exception
{
	private static final long serialVersionUID = -960091158805232282L;

	public PointSortieException()
	{
		super();
	}
	
	public PointSortieException(String m)
	{
		super(m);
	}

}
