/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package kraken.exceptions;

/**
 * Exception levée par le D* Lite
 * 
 * @author pf
 *
 */

public class DStarLiteException extends Exception
{

	private static final long serialVersionUID = -960091158805232282L;

	public DStarLiteException()
	{
		super();
	}

	public DStarLiteException(String m)
	{
		super(m);
	}

}
