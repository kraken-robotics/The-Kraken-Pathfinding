/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.kraken.utils;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

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
	
	private static final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
	private static final NumberFormat formatter;
	
	static
	{
		symbols.setDecimalSeparator('.');
		formatter = new DecimalFormat("#0.00", symbols);
	}

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
	
	public static double angleDifference(double angle1, double angle2)
	{
		double deltaO = (angle1 - angle2) % (2 * Math.PI);
		if(deltaO > Math.PI)
			deltaO -= 2 * Math.PI;
		else if(deltaO < -Math.PI)
			deltaO += 2 * Math.PI;
		return deltaO;
	}
	
	@Override
	public final String toString()
	{
		return position.toString() + ", o : " + formatter.format(orientation);
	}
}
