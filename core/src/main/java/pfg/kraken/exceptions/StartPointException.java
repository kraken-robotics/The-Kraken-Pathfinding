/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.exceptions;

/**
 * Exception thrown when the start point causes an exception
 * 
 * @author pf
 *
 */

public class StartPointException extends NoPathException
{

	private static final long serialVersionUID = -960091158805232282L;

	public StartPointException()
	{
		super();
	}

	public StartPointException(String m)
	{
		super(m);
	}
	
	public StartPointException(String m, Throwable e)
	{
		super(m, e);
	}	

}
