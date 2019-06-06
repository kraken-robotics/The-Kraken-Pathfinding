/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.exceptions;

/**
 * Exception thrown when the end point causes an exception
 * 
 * @author pf
 *
 */

public class EndPointException extends NoPathException
{

	private static final long serialVersionUID = -960091158805232282L;

	public EndPointException()
	{
		super();
	}

	public EndPointException(String m)
	{
		super(m);
	}
	
	public EndPointException(String m, Throwable e)
	{
		super(m, e);
	}	

}
