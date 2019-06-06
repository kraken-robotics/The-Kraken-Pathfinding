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

public class NotFastEnoughException extends NoPathException
{

	private static final long serialVersionUID = -960091158805232282L;

	public NotFastEnoughException()
	{
		super();
	}

	public NotFastEnoughException(String m)
	{
		super(m);
	}

	public NotFastEnoughException(String m, Throwable e)
	{
		super(m, e);
	}	

}
