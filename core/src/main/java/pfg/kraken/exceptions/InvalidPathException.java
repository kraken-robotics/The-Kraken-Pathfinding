/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.exceptions;

/**
 * Exception thrown when a user path is invalid
 * 
 * @author pf
 *
 */

public class InvalidPathException extends PathfindingException
{

	private static final long serialVersionUID = -960091158805232282L;

	public InvalidPathException()
	{
		super();
	}

	public InvalidPathException(String m)
	{
		super(m);
	}

	public InvalidPathException(String m, Throwable e)
	{
		super(m, e);
	}	

}
