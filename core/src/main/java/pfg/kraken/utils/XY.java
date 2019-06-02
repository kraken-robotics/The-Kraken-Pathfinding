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
 * Vecteur en lecture seule
 * 
 * @author pf
 *
 */

public class XY implements Serializable
{
	private static final long serialVersionUID = 1L;
	protected volatile double x;
	protected volatile double y;
	private static final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
	private static final NumberFormat formatter;
	
	static
	{
		symbols.setDecimalSeparator('.');
		formatter = new DecimalFormat("#0.00", symbols);
	}

	@SuppressWarnings("unused")
	public XY(double longueur, double angle, boolean useless)
	{
		x = Math.cos(angle) * longueur;
		y = Math.sin(angle) * longueur;
	}

	public XY(double requestedX, double requestedY)
	{
		x = requestedX;
		y = requestedY;
	}

	public final double dot(XY other)
	{
		return x * other.x + y * other.y;
	}

	public final XY_RW plusNewVector(XY other)
	{
		return new XY_RW(x + other.x, y + other.y);
	}

	public final XY_RW minusNewVector(XY other)
	{
		return new XY_RW(x - other.x, y - other.y);
	}

	@Override
	public final XY_RW clone()
	{
		return new XY_RW(this.x, this.y);
	}

	public final double squaredDistance(XY other)
	{
		double tmp_x = x - other.x, tmp_y = y - other.y;
		return tmp_x * tmp_x + tmp_y * tmp_y;
	}

	public final double distance(XY other)
	{
		return Math.sqrt((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y));
	}

	// Renvoie une approximation de la distance. Très rapide
	public final double distanceFast(XY other)
	{
		double dx = Math.abs(x - other.x);
		double dy = Math.abs(y - other.y);
		return Math.max(dx, dy) + 0.414 * Math.min(dx, dy);
	}

	@Override
	public final String toString()
	{
		return "(" + formatter.format(x) + "," + formatter.format(y) + ")";
	}

	@Override
	public final boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		else if(obj == null)
			return false;
		else if(!(obj instanceof XY))
			return false;

		XY other = (XY) obj;
		if(x != other.x || (y != other.y))
			return false;
		return true;
	}
	
	/**
	 * Returns true iff the segment (pointA1, pointA2) intersects the segment (pointB1, pointB2)
	 * @param pointA1
	 * @param pointA2
	 * @param pointB1
	 * @param pointB2
	 * @return
	 */
	public static boolean segmentIntersection(XY pointA1, XY pointA2, XY pointB1, XY pointB2)
	{
		// Source : https://stackoverflow.com/questions/3746274/line-intersection-with-aabb-rectangle
		
	    XY b = pointA2.minusNewVector(pointA1);
	    XY d = pointB2.minusNewVector(pointB1);
	    double bDotDPerp = b.getX() * d.getY() - b.getY() * d.getX();

	    // if b dot d == 0, it means the lines are parallel so have infinite intersection points
	    if(bDotDPerp == 0)
	        return false;

	    XY c = pointB1.minusNewVector(pointA1);
	    double t = (c.getX() * d.getY() - c.getY() * d.getX()) / bDotDPerp;
	    if(t < 0 || t > 1)
	        return false;

	    double u = (c.getX() * b.getY() - c.getY() * b.getX()) / bDotDPerp;
	    if(u < 0 || u > 1)
	        return false;

	    return true;
	}

	/**
	 * Copie this dans other.
	 * 
	 * @param other
	 */
	public final void copy(XY_RW other)
	{
		other.x = x;
		other.y = y;
	}

	public final XY_RW rotateNewVector(double angle, XY centreRotation)
	{
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		return new XY_RW(cos * (x - centreRotation.x) - sin * (y - centreRotation.y) + centreRotation.x, sin * (x - centreRotation.x) + cos * (y - centreRotation.y) + centreRotation.y);
	}

	public final double getArgument()
	{
		return Math.atan2(y, x);
	}

	public final double getFastArgument()
	{
		// http://math.stackexchange.com/questions/1098487/atan2-faster-approximation
		double a = Math.min(Math.abs(x), Math.abs(y)) / Math.max(Math.abs(x), Math.abs(y));
		double s = a * a;
		double r = ((-0.0464964749 * s + 0.15931422) * s - 0.327622764) * s * a + a;
		if(Math.abs(y) > Math.abs(x))
			r = 1.57079637 - r;
		if(x < 0)
			r = 3.14159274 - r;
		if(y < 0)
			r = -r;
		return r;
	}

	@Override
	public int hashCode()
	{
		return (int) Math.round((x + 1500) * 2000 + y);
	}

	public final double getX()
	{
		return x;
	}

	public final double getY()
	{
		return y;
	}

	/**
	 * La norme du vecteur
	 * 
	 * @return
	 */
	public double norm()
	{
		return Math.sqrt(x * x + y * y);
	}
	
	public double squaredNorm()
	{
		return x * x + y * y;
	}
	
	/**
	 * The distance is in μm !
	 * @param other
	 * @return
	 */
	public final int distanceOctile(XY other)
	{
		double dx = Math.abs(x - other.x);
		double dy = Math.abs(y - other.y);
		return (int) (1000 * Math.max(dx, dy) + 414 * Math.min(dx, dy));
	}

}
