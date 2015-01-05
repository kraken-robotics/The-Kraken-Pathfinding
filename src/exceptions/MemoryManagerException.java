package exceptions;

/**
 * Exception levée par le memory manager
 * @author pf
 *
 */

public class MemoryManagerException extends Exception
{

	private static final long serialVersionUID = -960091158805232282L;

	public MemoryManagerException()
	{
		super();
	}
	
	public MemoryManagerException(String m)
	{
		super(m);
	}

}
