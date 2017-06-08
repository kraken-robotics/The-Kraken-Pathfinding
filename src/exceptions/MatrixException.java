package exceptions;

/**
 * Exception levée en cas de calculs matriciels impossibles
 * @author pf
 *
 */

public class MatrixException  extends Exception
{

	private static final long serialVersionUID = -7968975910907981869L;

	public MatrixException()
	{
		super();
	}
	
	public MatrixException(String m)
	{
		super(m);
	}
	

}
