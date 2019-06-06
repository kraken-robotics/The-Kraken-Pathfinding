/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.exceptions;

/**
 * Exception thrown when no path is found
 * 
 * @author pf
 *
 */

public class TimeoutException extends NoPathException
{

	private static final long serialVersionUID = -960091158805232282L;

	public TimeoutException()
	{
		super();
	}

	public TimeoutException(String m)
	{
		super(m);
	}

	public TimeoutException(String m, Throwable e)
	{
		super(m, e);
	}	

}
