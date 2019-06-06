/*
 * Copyright (C) 2013-2019 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.exceptions;

/**
 * Exception thrown when the pathfinding isn't initialized
 * 
 * @author pf
 *
 */

public class NotInitializedException extends PathfindingException
{

	private static final long serialVersionUID = -960091158805232282L;

	public NotInitializedException()
	{
		super();
	}

	public NotInitializedException(String m)
	{
		super(m);
	}

	public NotInitializedException(String m, Throwable e)
	{
		super(m, e);
	}	

}
