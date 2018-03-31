/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.exceptions;

/**
 * Exception thrown when the mode is unknown
 * 
 * @author pf
 *
 */

public class UnknownModeException extends NoPathException
{

	private static final long serialVersionUID = -960091158805232282L;

	public UnknownModeException()
	{
		super();
	}

	public UnknownModeException(String m)
	{
		super(m);
	}

}
