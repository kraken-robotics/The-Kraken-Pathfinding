/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.exceptions;

/**
 * Exception thrown when the pathfinding isn't initialized
 * 
 * @author pf
 *
 */

public class NotInitializedPathfindingException extends PathfindingException
{

	private static final long serialVersionUID = -960091158805232282L;

	public NotInitializedPathfindingException()
	{
		super();
	}

	public NotInitializedPathfindingException(String m)
	{
		super(m);
	}

	public NotInitializedPathfindingException(String m, Throwable e)
	{
		super(m, e);
	}	

}
