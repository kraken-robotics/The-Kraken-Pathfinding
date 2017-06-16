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
	public final XY position;
	public final double orientation;
	
	public XYO(double x, double y, double orientation)
	{
		this(new XY(x,y), orientation);
	}
	
	public XYO(XY position, double orientation)
	{
		this.position = position;
		this.orientation = orientation;
	}
}
