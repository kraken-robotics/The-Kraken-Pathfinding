/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.exceptions;

/**
 * Exception thrown when no path is found
 * 
 * @author pf
 *
 */

public class NoPathException extends PathfindingException
{

	private static final long serialVersionUID = -960091158805232282L;

	public NoPathException()
	{
		super();
	}

	public NoPathException(String m)
	{
		super(m);
	}

	public NoPathException(String m, Throwable e)
	{
		super(m, e);
	}	

}
