/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.utils;

import java.io.Serializable;

/**
 * A structure with a position (x,y) and an orientation
 * @author pf
 *
 */

public class XYO implements Serializable
{
	private static final long serialVersionUID = -1530328162896631206L;
	public final XY_RW position;
	public double orientation;
	
	public XYO(double x, double y, double orientation)
	{
		this(new XY_RW(x,y), orientation);
	}
	
	public XYO(XY_RW position, double orientation)
	{
		this.position = position;
		this.orientation = orientation;
	}
	
	public void copy(XYO other)
	{
		other.orientation = orientation;
		position.copy(other.position);
	}
	
	public void copy(XY_RW other)
	{
		position.copy(other);
	}
}
